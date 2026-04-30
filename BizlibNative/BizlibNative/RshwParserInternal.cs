using System.Runtime.InteropServices;
using System.Formats.Nrbf;

/** All the rshw arrays and how many elements they have
 * (note: *Len is the array length, not the pointer size in memory)
 */
[StructLayout(LayoutKind.Sequential)]
public struct RshwData {
    public IntPtr audioPtr; public int audioLen;
    public IntPtr signalPtr; public int signalLen;
    public IntPtr videoPtr; public int videoLen;
    public IntPtr errorPtr;
    public static RshwData Error(string err) => new RshwData { errorPtr = Marshal.StringToHGlobalAnsi(err) };
}

public static class RshwParserInternal {
    const string SignalField = "<signalData>k__BackingField";
    const string AudioField = "<audioData>k__BackingField";
    const string VideoField = "<videoData>k__BackingField";
    const string SignalKeyword = "signal";
    const string AudioKeyword = "audio";
    const string VideoKeyword = "video";

    public static unsafe RshwData _Read(Stream stream) {
        var result = new RshwData();

        try {
            var root = NrbfDecoder.DecodeClassRecord(stream);

            void ProcessArray<T>(string fieldKeyword, string field, ref IntPtr targetPtr, ref int targetLen, bool optional = false) where T: unmanaged {
                // Finding the name
                string? name = null;
                if (root.HasMember(field)) {
                    name = field;
                } else if (!optional) {
                    name = root.MemberNames.FirstOrDefault(m => m.Contains(fieldKeyword));
                }
                if (name == null) {
                    if (!optional) result = RshwData.Error($"Field '{field}' was not found");
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

            ProcessArray<byte>(AudioKeyword, AudioField, ref result.audioPtr, ref result.audioLen);
            ProcessArray<int>(SignalKeyword, SignalField, ref result.signalPtr, ref result.signalLen);
            ProcessArray<byte>(VideoKeyword, VideoField, ref result.videoPtr, ref result.videoLen, optional: true);
        } catch (Exception e) {
            result = RshwData.Error(e.ToString());
        }
        return result;
    }
}