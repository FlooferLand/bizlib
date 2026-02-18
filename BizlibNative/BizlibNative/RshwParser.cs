using System.Runtime.InteropServices;
using System.Formats.Nrbf;

namespace BizlibNative;

/** All the rshw arrays and how many elements they have
 * (note: *Len is the array length, not the pointer size in memory)
 */
[StructLayout(LayoutKind.Sequential)]
public struct RshwData {
    public IntPtr audioPtr; public int audioLen;
    public IntPtr signalPtr; public int signalLen;
    public IntPtr videoPtr; public int videoLen;
    public bool hasError;
}

public partial class Main {
    [UnmanagedCallersOnly(EntryPoint = "ReadRshw")]
    public static unsafe RshwData ReadRshw(IntPtr handle, int size) {
        if (handle == IntPtr.Zero) {
            Console.WriteLine("BizlibNative received a null pointer instead of a show");
            return new RshwData { hasError = true };
        }
        using var stream = new UnmanagedMemoryStream((byte*)handle.ToPointer(), size);
        return _Read(stream);
    }

    [UnmanagedCallersOnly(EntryPoint = "ReadRshwFile")]
    public static RshwData ReadRshwFile(IntPtr pathPtr) {
        string? path = Marshal.PtrToStringAnsi(pathPtr);
        if (string.IsNullOrEmpty(path) || !File.Exists(path))
            return new RshwData { hasError = true };

        using var  stream = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.Read, 4096, FileOptions.SequentialScan);
        return _Read(stream);
    }

    static unsafe RshwData _Read(Stream stream) {
        var result = new RshwData();

        try {
            var root = NrbfDecoder.DecodeClassRecord(stream);

            void ProcessArray<T>(string s, ref IntPtr targetPtr, ref int targetLen, bool optional = false) where T: unmanaged {
                // Finding the name
                string field = $"<{s}Data>k__BackingField";
                string? name = null;
                if (root.HasMember(field)) {
                    name = field;
                } else if (!optional) {
                    name = root.MemberNames.FirstOrDefault(m => m.Contains(s));
                }
                if (name == null) {
                    if (!optional) result.hasError = true;
                    return;
                }

                if (root.GetArrayRecord(name) is SZArrayRecord<T> record) {
                    var managedArray = record.GetArray();
                    int len = managedArray.Length;
                    int byteCount = len * sizeof(T);

                    IntPtr ptr = Marshal.AllocHGlobal(byteCount);
                    fixed (T* pSource = managedArray) {
                        Buffer.MemoryCopy(pSource, (void*)ptr, byteCount, byteCount);
                    }

                    targetPtr = ptr;
                    targetLen = len;
                }
            }

            Parallel.Invoke(
                () => ProcessArray<byte>("audio", ref result.audioPtr, ref result.audioLen),
                () => ProcessArray<int>("signal", ref result.signalPtr, ref result.signalLen),
                () => ProcessArray<byte>("video", ref result.videoPtr, ref result.videoLen, optional:true)
            );
        } catch (Exception e) {
            Console.WriteLine($"BizlibNative encountered an exception: {e}");
            result.hasError = true;
        }
        return result;
    }

    /** Cleans up the unmanaged show data memory */
    [UnmanagedCallersOnly(EntryPoint = "FreeRshw")]
    public static void FreeRshw(RshwData data) {
        if (data.audioPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.audioPtr);
        if (data.signalPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.signalPtr);
        if (data.videoPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.videoPtr);
    }
}