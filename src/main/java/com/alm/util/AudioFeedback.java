package com.alm.util;

import javax.sound.sampled.*;

/**
 * Audio feedback utility for the reception dashboard.
 * Synthesizes pleasant tones programmatically — no external WAV files needed.
 */
public class AudioFeedback {

    private static final float SAMPLE_RATE = 44100f;

    /**
     * Play a pleasant ascending two-tone chime (scan success).
     * Notes: C5 (523 Hz) → E5 (659 Hz), 120ms each.
     */
    public static void playScanSuccess() {
        playAsync(() -> {
            playTone(523.25, 120, 0.5); // C5
            playTone(659.25, 150, 0.5); // E5
        });
    }

    /**
     * Play a triumphant three-tone chime (check-in confirmed).
     * Notes: C5 → E5 → G5, building up.
     */
    public static void playCheckInSuccess() {
        playAsync(() -> {
            playTone(523.25, 100, 0.45); // C5
            playTone(659.25, 100, 0.50); // E5
            playTone(783.99, 200, 0.55); // G5
        });
    }

    /**
     * Play a descending two-tone (check-out / farewell).
     * Notes: G5 → C5.
     */
    public static void playCheckOut() {
        playAsync(() -> {
            playTone(783.99, 120, 0.4); // G5
            playTone(523.25, 180, 0.4); // C5
        });
    }

    /**
     * Play a short low buzz (error / denied).
     */
    public static void playError() {
        playAsync(() -> {
            playTone(220.0, 200, 0.4); // A3 — low warning
        });
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private static void playAsync(Runnable soundJob) {
        Thread t = new Thread(soundJob, "AudioFeedback");
        t.setDaemon(true);
        t.start();
    }

    private static void playTone(double freqHz, int durationMs, double volume) {
        try {
            int numSamples = (int) (SAMPLE_RATE * durationMs / 1000.0);
            byte[] buf = new byte[numSamples * 2]; // 16-bit mono

            // Fade in/out envelope to avoid click artifacts
            int fadeLen = Math.min(numSamples / 6, 800);

            for (int i = 0; i < numSamples; i++) {
                double angle = 2.0 * Math.PI * i * freqHz / SAMPLE_RATE;
                double sample = Math.sin(angle);

                // Smooth envelope (fade in + fade out)
                double envelope = 1.0;
                if (i < fadeLen)
                    envelope = (double) i / fadeLen;
                else if (i > numSamples - fadeLen)
                    envelope = (double) (numSamples - i) / fadeLen;

                short val = (short) (sample * envelope * volume * Short.MAX_VALUE);
                buf[i * 2] = (byte) (val & 0xFF);
                buf[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
            }

            AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                line.open(fmt);
                line.start();
                line.write(buf, 0, buf.length);
                line.drain();
            }
        } catch (Exception e) {
            // Silently ignore audio errors — non-critical feature
        }
    }
}
