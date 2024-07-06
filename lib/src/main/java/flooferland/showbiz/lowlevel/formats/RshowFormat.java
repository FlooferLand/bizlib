package flooferland.showbiz.lowlevel.formats;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.IShowFormat;
import flooferland.showbiz.lowlevel.show.ShowData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Ridiculous class thrown together by Creativious
 * Explanation: <a href="https://discord.com/channels/@me/1254172388208414773/1256538810968313997">(Discord DM)</a>
 * - FL
 */
public class RshowFormat implements IShowFormat {
    private static final int EOF = 11;
    private static final int BYTE_IDENTIFIER = 2;
    private static final int _32BIT_INTEGER_IDENTIFIER = 8;
    private static final byte[] targetBytes;

    // Allows for skipping the whole first "BackingField"s section
    // by searching for this pattern since it shows up in every BinaryFormatter file
    // - FL
    static {
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.put((byte) 0x07);
        buffer.put((byte) 0x07);
        buffer.put((byte) 0x07);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x08);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x02);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x09);
        buffer.put((byte) 0x03);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x09);
        buffer.put((byte) 0x04);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        targetBytes = buffer.array();
    }
    
    // 90% of the code are checks. Could probably be cut down using recursion - FL
    public Result<ShowData, String> readFromStream(InputStream stream) {
        byte[] bytes;
        try {
            bytes = stream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte[] tempBytes = new byte[20];
        boolean filling_temp_bytes = false;
        boolean found_starting_point = false;
        boolean got_byte_after_starting_point = false;
        byte start_point_byte = 0;
        int t_i = 0;
        int start_type = 0;
        boolean at_audio_data = false;
        boolean at_signal_data = false;
        boolean at_video_data = false;
        int skip_amount = 0;
        int capture_amount = 0;
        boolean skipping = false;
        boolean start_type_found = false;
        boolean capturing = false;
        boolean found_audio_count = false;
        boolean found_signal_count = false;
        boolean has_video = false;
        boolean found_video_count = false;
        int int32_capture_counter = 0;
        int audio_count = 0;
        int signal_count = 0;
        int video_count = 0;
        boolean capturing_int32 = false;
        byte[] audio_data = new byte[0];
        int[] signal_data = new int[0];
        byte[] video_data = new byte[0];
        byte[] int32_capture_bytes = new byte[4];
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if (!found_starting_point) {
                if (b == (byte) 0x07) {
                    tempBytes[t_i] = b;
                    filling_temp_bytes = true;
                    t_i++;
                }
                else if (filling_temp_bytes) {
                    tempBytes[t_i] = b;
                    t_i++;
                    if (t_i == 20) {
                        if (Arrays.equals(tempBytes, targetBytes)) {
                            filling_temp_bytes = false;
                            found_starting_point = true;
                            t_i = 0;
                        }
                        else {
                            t_i = 0;
                            filling_temp_bytes = false;
                        }
                    }
                }
                continue;
            }
            if (!start_type_found) {
                if (!got_byte_after_starting_point) {
                    start_point_byte = b;
                    got_byte_after_starting_point = true;
                }
                if (start_point_byte == (byte) 10 && b == (byte) 15) {
                    // start type 1
                    start_type = 1;
                    start_type_found = true;
                    skipping = true;
                    skip_amount = 5;
                    at_audio_data = true;
                }
                else if (start_point_byte == (byte) 9 && b == (byte) 5) {
                    skipping = true;
                    start_type = 2;
                    skip_amount = 9;
                    start_type_found = true;
                    at_audio_data = true;
                }
            }
            if (skipping) {
                skip_amount--;
                if (skip_amount == 0) {
                    skipping = false;
                }
                continue;
            }
            if (at_audio_data && !capturing && !found_audio_count) {
                int32_capture_bytes[int32_capture_counter] = b;
                int32_capture_counter++;
                if (int32_capture_counter == 4) {
                    int32_capture_counter = 0;
                    audio_count = getIntFromCsharpBytes(int32_capture_bytes);
                    found_audio_count = true;
                    skipping = true;
                    skip_amount = 1;
                    audio_data = new byte[audio_count];
                    capturing = true;
                    capture_amount = audio_count;
                }
                continue;
            }
            if (capturing && at_audio_data) {
                audio_data[audio_count - capture_amount] = b;
                capture_amount--;
                if (capture_amount == 0) {
                    capturing = false;
                }
                continue;
            }
            if (!capturing && at_audio_data) {
                at_audio_data = false;
                at_signal_data = true;
                skipping = true;
                skip_amount = 4;
                continue;
            }
            if (at_signal_data && !capturing && !found_signal_count) {
                int32_capture_bytes[int32_capture_counter] = b;
                int32_capture_counter++;
                if (int32_capture_counter == 4) {
                    int32_capture_counter = 0;
                    signal_count = getIntFromCsharpBytes(int32_capture_bytes);
                    found_signal_count = true;
                    skipping = true;
                    skip_amount = 1;
                    signal_data = new int[signal_count];
                    capturing = true;
                    capture_amount = signal_count;
                }
                continue;
            }
            if (capturing && at_signal_data) {
                int32_capture_bytes[int32_capture_counter] = b;
                int32_capture_counter++;
                if (int32_capture_counter == 4) {
                    int32_capture_counter = 0;
                    signal_data[signal_count - capture_amount] = getIntFromCsharpBytes(int32_capture_bytes);
                    capture_amount--;
                    if (capture_amount == 0) {
                        capturing = false;
                    }
                }
                continue;
            }
            if (!capturing && at_signal_data) {
                at_signal_data = false;
                at_video_data = true;
                if (b == (byte) 11) {
                    has_video = false;
                    break;
                }
                else {
                    has_video = true;
                }
                skipping = true;
                skip_amount = 4;
                continue;
            }
            if (!capturing && at_video_data && !found_video_count) {
                int32_capture_bytes[int32_capture_counter] = b;
                int32_capture_counter++;
                if (int32_capture_counter == 4) {
                    int32_capture_counter = 0;
                    video_count = getIntFromCsharpBytes(int32_capture_bytes);
                    found_video_count = true;
                    skipping = true;
                    skip_amount = 1;
                    video_data = new byte[video_count];
                    capturing = true;
                    capture_amount = video_count;
                }
                continue;
            }
            if (capturing && at_video_data) {
                video_data[video_count - capture_amount] = b;
                capture_amount--;
                if (capture_amount == 0) {
                    capturing = false;
                }
                continue;
            }
            if (!capturing && at_video_data) {
                at_video_data = false;
                if (b != (byte) 11) {
                    System.out.println("For some reason the data seems to go on past expectations, ignoring");
                    break;
                }
            }
        }
        if (!has_video) {
            System.out.println("There is no video with this .rshw file");
        }
        return Result.ok(new ShowData(signal_data, audio_data, video_data));
    }

    @Override
    public void writeToStream(ShowData format, OutputStream stream) {
        throw new RuntimeException("Not implemented");
    }

    private static int getIntFromCsharpBytes(byte[] bytes) {
        return (bytes[0] & 0xFF) | ((bytes[1] & 0xFF) << 8) | ((bytes[2] & 0xFF) << 16) | ((bytes[3] & 0xFF) << 24);
    }
}
