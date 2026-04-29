using System.Runtime.InteropServices;
using System.Formats.Nrbf;

public partial class Main {
    [UnmanagedCallersOnly(EntryPoint = "ReadRshw")]
    public static unsafe RshwData ReadRshw(IntPtr handle, int size) {
        if (handle == IntPtr.Zero) {
            Console.WriteLine("BizlibNative received a null pointer instead of a show");
            return new RshwData { hasError = true };
        }
        using var stream = new UnmanagedMemoryStream((byte*)handle.ToPointer(), size);
        return RshwParserInternal._Read(stream);
    }

    [UnmanagedCallersOnly(EntryPoint = "ReadRshwFile")]
    public static RshwData ReadRshwFile(IntPtr pathPtr) {
        string? path = Marshal.PtrToStringAnsi(pathPtr);
        if (string.IsNullOrEmpty(path) || !File.Exists(path))
            return RshwData.Error("File does not exist");

        using var  stream = new FileStream(path, FileMode.Open, FileAccess.Read, FileShare.Read, 4096, FileOptions.SequentialScan);
        return RshwParserInternal._Read(stream);
    }

    /** Cleans up the unmanaged show data memory */
    [UnmanagedCallersOnly(EntryPoint = "FreeRshw")]
    public static void FreeRshw(RshwData data) {
        if (data.audioPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.audioPtr);
        if (data.signalPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.signalPtr);
        if (data.videoPtr != IntPtr.Zero) Marshal.FreeHGlobal(data.videoPtr);
    }
}