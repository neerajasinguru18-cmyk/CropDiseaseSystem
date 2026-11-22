
package com.cropdisease.backend;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.OnnxTensor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

@Service
public class DiseaseDetectionService {

    private OrtEnvironment environment;
    private OrtSession session;
    private List<String> diseaseClasses;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final int IMAGE_SIZE = 224;

    private static final float[] MEAN = {
        0.485f, 0.456f, 0.406f
    };

    private static final float[] STD = {
        0.229f, 0.224f, 0.225f
    };

    @PostConstruct
    public void loadModel() {
        try {
            InputStream modelStream =
                    getClass().getResourceAsStream("/models/cropguard.onnx");

            if (modelStream == null) {
                throw new IllegalStateException("AI model file not found!");
            }

            byte[] modelBytes;
            try (modelStream) {
                modelBytes = modelStream.readAllBytes();
            }

            environment = OrtEnvironment.getEnvironment();

            try (OrtSession.SessionOptions options =
                    new OrtSession.SessionOptions()) {
                session = environment.createSession(modelBytes, options);
            }

            InputStream classStream =
                    getClass().getResourceAsStream("/models/classes.json");

            if (classStream == null) {
                throw new IllegalStateException("Disease labels file not found!");
            }

            try (classStream) {
                diseaseClasses = mapper.readValue(
                        classStream,
                        new TypeReference<List<String>>() {}
                );
            }

            System.out.println("AI Disease Model Loaded Successfully!");
            System.out.println("Total disease classes: " + diseaseClasses.size());

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load AI disease detection model!", e);
        }
    }

    public PredictionResult predict(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a leaf image.");
        }

        BufferedImage image = ImageIO.read(file.getInputStream());

        if (image == null) {
            throw new IllegalArgumentException("Invalid image file.");
        }

        FloatBuffer buffer = preprocessImage(image);

        long[] shape = {1, 3, IMAGE_SIZE, IMAGE_SIZE};

        try (OnnxTensor tensor =
                    OnnxTensor.createTensor(environment, buffer, shape);
             OrtSession.Result result =
                    session.run(Map.of("input", tensor))) {

            float[][] output = (float[][]) result.get("logits")
                    .get().getValue();

            float[] probabilities = softmax(output[0]);

            int bestIndex = 0;

            for (int i = 1; i < probabilities.length; i++) {
                if (probabilities[i] > probabilities[bestIndex]) {
                    bestIndex = i;
                }
            }

            String disease = diseaseClasses.get(bestIndex);
            double confidence = probabilities[bestIndex] * 100.0;

            return new PredictionResult(disease, confidence);
        }
    }

    private FloatBuffer preprocessImage(BufferedImage original) {

        int width = original.getWidth();
        int height = original.getHeight();

        int newWidth;
        int newHeight;

        if (width < height) {
            newWidth = 256;
            newHeight = (int) Math.round(height * 256.0 / width);
        } else {
            newHeight = 256;
            newWidth = (int) Math.round(width * 256.0 / height);
        }

        BufferedImage resized =
                new BufferedImage(newWidth, newHeight,
                        BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        graphics.drawImage(original, 0, 0, newWidth, newHeight, null);
        graphics.dispose();

        int startX = (newWidth - IMAGE_SIZE) / 2;
        int startY = (newHeight - IMAGE_SIZE) / 2;

        FloatBuffer buffer = FloatBuffer.allocate(
                3 * IMAGE_SIZE * IMAGE_SIZE);

        for (int channel = 0; channel < 3; channel++) {

            for (int y = 0; y < IMAGE_SIZE; y++) {

                for (int x = 0; x < IMAGE_SIZE; x++) {

                    int rgb = resized.getRGB(startX + x, startY + y);

                    float value;

                    if (channel == 0) {
                        value = ((rgb >> 16) & 0xFF) / 255.0f;
                    } else if (channel == 1) {
                        value = ((rgb >> 8) & 0xFF) / 255.0f;
                    } else {
                        value = (rgb & 0xFF) / 255.0f;
                    }

                    value = (value - MEAN[channel]) / STD[channel];

                    buffer.put(value);
                }
            }
        }

        buffer.rewind();
        return buffer;
    }

    private float[] softmax(float[] logits) {

        float max = logits[0];

        for (float value : logits) {
            if (value > max) {
                max = value;
            }
        }

        float[] probabilities = new float[logits.length];
        double sum = 0;

        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - max);
            sum += probabilities[i];
        }

        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] /= (float) sum;
        }

        return probabilities;
    }

    public OrtSession getSession() {
        return session;
    }

    public OrtEnvironment getEnvironment() {
        return environment;
    }

    public List<String> getDiseaseClasses() {
        return diseaseClasses;
    }

    @PreDestroy
    public void closeModel() {
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
