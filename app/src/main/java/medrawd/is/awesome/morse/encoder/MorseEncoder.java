package medrawd.is.awesome.morse.encoder;

import static android.media.AudioManager.STREAM_MUSIC;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class MorseEncoder extends Thread {
    private static final String TAG = MorseEncoder.class.getSimpleName();

    private static final double DOT = 0.100, DASH = DOT * 3;
    private static final int FREQ = 800;
    private static String[] MORSE_ALPHABET = {".-", "-...", "-.-.", "-..", ".", "..-.", "--.", "....", "..", ".---", "-.-", ".-..", "--", "-.", "---", ".--.", "--.-", ".-.", "...", "-", "..-", "...-", ".--", "-..-", "-.--", "--.."};
    private static String[] MORSE_NUMBERS = {"-----", ".----", "..---", "...--", "....-", ".....", "-....", "--...", "---..", "----."};

    final Queue<Character> input = new LinkedBlockingQueue<>();
    boolean stop = false;
    private final AudioTrack audioTrack;
    private final float[] dotSamples;
    private final float[] dashSamples;
    private final float[] symbolSeparatorSamples;

    public void softStop() {
        stop = true;

        audioTrack.stop();
        audioTrack.release();
    }

    public void post(char c) {
        input.add(c);
        synchronized (input) {
            input.notify();
        }
    }

    public void post(String string) {
        for (char c: string.toCharArray()){
            input.add(c);
        }
        synchronized (input) {
            input.notify();
        }
    }

    public void post(CharSequence sequence) {
        for (int i = 0; i< sequence.length(); i++){
            char c = sequence.charAt(i);
            input.add(c);
        }
        synchronized (input) {
            input.notify();
        }
    }

    public MorseEncoder() {
        super();
        int sampleRate = AudioTrack.getNativeOutputSampleRate(STREAM_MUSIC);
        int minBufferSizeBytes = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT);

        int numSamplesDot = (int) (DOT * sampleRate);
        dotSamples = new float[numSamplesDot];
        for (int i = 0; i < numSamplesDot; ++i){
            dotSamples[i] = (float) Math.sin(2 * Math.PI * i / (sampleRate / FREQ)); // Sine wave
        }

        int numSamplesDash = (int) (DASH * sampleRate);
        dashSamples = new float[numSamplesDash];
        for (int i = 0; i < numSamplesDash; ++i){
            dashSamples[i] = (float) Math.sin(2 * Math.PI * i / (sampleRate / FREQ)); // Sine wave
        }

        int numSamplesSymbolSeparator = (int) (DOT * 0.8 * sampleRate);
        symbolSeparatorSamples = new float[numSamplesSymbolSeparator];

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                minBufferSizeBytes,
                AudioTrack.MODE_STREAM);
        audioTrack.play();
        start();
    }

    @Override
    public void run() {
        super.run();
        while (!stop) {
            while (input.isEmpty()) {
                synchronized (input) {
                    try {
                        input.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            while(null != input.peek()){
                char c = input.poll();
                StringBuilder builder = new StringBuilder();
                builder.append(c);
                builder.append(": ");
                if(Character.isSpaceChar(c)){
                    for(int i = 0; i<7; i++){
                        audioTrack.write(symbolSeparatorSamples, 0, symbolSeparatorSamples.length, AudioTrack.WRITE_BLOCKING);
                    }
                } else {
                    for (char note : (Character.isLetterOrDigit(c) ? (Character.isLetter(c)?MORSE_ALPHABET[Character.toUpperCase(c) - 'A'].toCharArray() : MORSE_NUMBERS[c - '0'].toCharArray()) : new char[]{'\n'})) {
                        builder.append(note == ' ' ? "\n" : note);
                        if (note == '.') {
                            audioTrack.write(dotSamples, 0, dotSamples.length, AudioTrack.WRITE_BLOCKING);
                        } else {
                            audioTrack.write(dashSamples, 0, dashSamples.length, AudioTrack.WRITE_BLOCKING);
                        }
                        audioTrack.write(symbolSeparatorSamples, 0, symbolSeparatorSamples.length, AudioTrack.WRITE_BLOCKING);
                    }
                    for(int i = 0; i<2; i++){
                        audioTrack.write(symbolSeparatorSamples, 0, symbolSeparatorSamples.length, AudioTrack.WRITE_BLOCKING);
                    }
                }
                Log.d(TAG, builder.toString());
            }
        }
    }
}
