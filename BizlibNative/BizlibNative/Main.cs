using System.Formats.Nrbf;
using System.Runtime.InteropServices;

namespace BizlibNative;

/** All the rshw arrays and how many elements they have
 * (note: *Len is the array length, not the pointer size in memory)
 */
[StructLayout(LayoutKind.Sequential)]
public struct RshwData {
    public IntPtr audioPtr; public int audioLen;
    public IntPtr signalPtr; public int signalLen;
    public IntPtr videoPtr; public int videoLen;
    public int statusCode;
}

public static class Main {
    [UnmanagedCallersOnly(EntryPoint = "ReadRshw")]
    public static unsafe RshwData ReadRshw(IntPtr handle, int size) {
        var result = new RshwData();
        if (handle == IntPtr.Zero) {
            Console.WriteLine("BizlibNative received a null pointer instead of a show");
            result.statusCode = -1;
            return result;
        }

        try {
            using var stream = new UnmanagedMemoryStream((byte*)handle.ToPointer(), size);
            var root = NrbfDecoder.DecodeClassRecord(stream);

            // TODO: These arrays *could* technically contain nulls as elements, maybe loop through and remove them first?
            T[]? GetArray<T>(string s, bool optional = false) where T: unmanaged {
                try {
                    string field = $"<{s}Data>k__BackingField";
                    string? name = null;
                    if (root.HasMember(field)) {
                        name = field;
                    } else if (!optional) {
                        name = root.MemberNames.First(m => m.Contains(s));
                    }
                    if (name == null) return null;

                    var record = root.GetArrayRecord(name);
                    if (record == null) return null;

                    return ((SZArrayRecord<T>) record).GetArray();
                } catch (Exception e) {
                    Console.WriteLine($"BizlibNative failed to read the '{s}' field: {e}");
                    return null;
                }
            }

            byte[]? audio = GetArray<byte>("audio");
            if (audio != null) {
                result.audioPtr = Marshal.AllocHGlobal(audio.Length);
                result.audioLen = audio.Length;
                Marshal.Copy(audio, 0, result.audioPtr, audio.Length);
            } else {
                result.statusCode = -1;
            }

            int[]? signal = GetArray<int>("signal");
            if (signal != null) {
                result.signalPtr = Marshal.AllocHGlobal(signal.Length * sizeof(int));
                result.signalLen = signal.Length;
                Marshal.Copy(signal, 0, result.signalPtr, signal.Length);
            } else {
                result.statusCode = -1;
            }

            byte[]? video = GetArray<byte>("video", optional:true);
            if (video != null) {
                result.videoPtr = Marshal.AllocHGlobal(video.Length);
                result.videoLen = video.Length;
                Marshal.Copy(video, 0, result.videoPtr, video.Length);
            }
        } catch (Exception e) {
            Console.WriteLine($"BizlibNative encountered an exception: {e}");
            result.statusCode = -1;
        }

        return result;
    }

    [UnmanagedCallersOnly(EntryPoint = "FreeRshw")]
    public static void FreeRshw(RshwData data) {
        if (data.audioPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.audioPtr);
        if (data.signalPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.signalPtr);
        if (data.videoPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.videoPtr);
    }
}