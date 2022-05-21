package correcter;

import java.io.*;
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Write a mode:");
        String mode = scanner.nextLine();
        switch (mode) {
            case "encode":
                encode();
                break;
            case "send":
                send();
                break;
            case "decode":
                decode();
                break;
            default:
                break;
        }
    }

    public static void encode() {
        System.out.println("send.txt:");
        String text = "";
        File file = new File("send.txt");
        try (Scanner sc = new Scanner(file)) {
            text = sc.nextLine();
        } catch (FileNotFoundException e) {
            System.out.println("ERROR");
        }
        System.out.println("text view: " + text);
        byte[] byteArray = text.getBytes();
        System.out.print("hex view:");
        for (byte b : byteArray) {
            System.out.print(" " + Integer.toHexString(b));
        }
        System.out.print("\nbin view:");
        StringBuilder bitsLine = new StringBuilder();
        for (byte b : byteArray) {
            String bin = String.format("%8s", Integer.toBinaryString(b)).replaceAll(" ", "0");
            bitsLine.append(bin);
            System.out.print(" " + bin);
        }
        System.out.println("\n\nencoded.txt:");
        System.out.print("expand:");
        String[] parityArray = new String[byteArray.length * 2];
        for (int i = 0, bit = 0; i < parityArray.length; i++, bit += 4) {
            StringBuilder expByte = new StringBuilder();
            expByte.append("..")
                   .append(bitsLine.charAt(bit))
                   .append(".")
                   .append(bitsLine.substring(bit + 1, bit + 4))
                   .append(".");
            System.out.print(" " + expByte);
            int p1 = (Integer.parseInt(expByte.substring(2, 3))
                    + Integer.parseInt(expByte.substring(4, 5))
                    + Integer.parseInt(expByte.substring(6, 7)))
                    % 2;
            int p2 = (Integer.parseInt(expByte.substring(2, 3))
                    + Integer.parseInt(expByte.substring(5, 6))
                    + Integer.parseInt(expByte.substring(6, 7)))
                    % 2;
            int p4 = (Integer.parseInt(expByte.substring(4, 5))
                    + Integer.parseInt(expByte.substring(5, 6))
                    + Integer.parseInt(expByte.substring(6, 7)))
                    % 2;
            expByte.replace(0, 1, String.valueOf(p1))
                   .replace(1, 2, String.valueOf(p2))
                   .replace(3, 4, String.valueOf(p4))
                   .replace(7, 8, "0");
            parityArray[i] = expByte.toString();
        }
        System.out.print("\nparity:");
        byte[] result = new byte[parityArray.length];
        for (int i = 0; i < parityArray.length; i++) {
            result[i] = (byte) (255 & Integer.parseInt(parityArray[i], 2));
            System.out.print(" " + parityArray[i]);
        }
        System.out.print("\nhex view:");
        for (String b : parityArray) {
            String dec = Integer.toHexString(Integer.parseInt(b, 2));
            System.out.print(" " + String.format("%2s", dec).replaceAll(" ", "0"));
        }
        file = new File("encoded.txt");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(result);
        } catch (IOException e) {
            System.out.println("ERROR");
        }
    }

    public static void send() {
        System.out.println("encoded.txt:");
        System.out.print("hex view:");
        File file = new File("encoded.txt");
        byte[] byteArray = new byte[0];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byteArray = inputStream.readAllBytes();
        } catch (IOException e) {
            System.out.println("ERROR");
        }
        for (byte b : byteArray) {
            String bin = String.format("%2s", Integer.toHexString(b)).replaceAll(" ", "0");
            System.out.print(" " + bin);
        }
        System.out.print("\nbin view:");
        for (byte b : byteArray) {
            String bin = String.format("%8s", Integer.toBinaryString(b)).replaceAll(" ", "0");
            System.out.print(" " + bin);
        }
        System.out.println("\n\nreceived.txt:");
        System.out.print("bin view:");
        Random random = new Random();
        for (int i = 0; i < byteArray.length; i++) {
            int k = random.nextInt(7);
            byteArray[i] = (byte) ((byteArray[i] ^ (byte) Math.pow(2, k)) % 128);
            String bin = String.format("%8s", Integer.toBinaryString(byteArray[i])).replaceAll(" ", "0");
            System.out.print(" " + bin);
        }
        System.out.print("\nhex view:");
        for (byte b : byteArray) {
            String bin = String.format("%2s", Integer.toHexString(b)).replaceAll(" ", "0");
            System.out.print(" " + bin);
        }
        file = new File("received.txt");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(byteArray);
        } catch (IOException e) {
            System.out.println("ERROR");
        }
    }

    public static void decode() {
        System.out.println("received.txt:");
        System.out.print("hex view:");
        byte[] byteArray = new byte[0];
        File file = new File("received.txt");
        try (FileInputStream inputStream = new FileInputStream(file)) {
            byteArray = inputStream.readAllBytes();
        } catch (IOException e) {
            System.out.println("ERROR");
        }
        int[] intArray = new int[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            intArray[i] = ((int) byteArray[i]) & 255;
            String bin = String.format("%2s", Integer.toHexString(intArray[i])).replaceAll(" ", "0");
            System.out.print(" " + bin);
        }
        System.out.print("\nbin view:");
        String[] correctedArray = new String[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            String bin = String.format("%8s", Integer.toBinaryString(intArray[i])).replaceAll(" ", "0");
            System.out.print(" " + bin);
            bin = fixByte(bin);
            correctedArray[i] = bin;
        }
        System.out.println("\n\ndecoded.txt:");
        System.out.print("correct:");
        StringBuilder bitsLine = new StringBuilder();
        for (String b : correctedArray) {
            System.out.print(" " + b);
            bitsLine.append(b, 2, 3).append(b, 4, 7);
        }
        System.out.print("\ndecode:");
        byte[] resultArray = new byte[correctedArray.length / 2];
        for (int i = 0, b = 0; b < bitsLine.length(); i++, b += 8) {
            String bin = bitsLine.substring(b, b + 8);
            System.out.print(" " + bin);
            resultArray[i] = (byte) Integer.parseInt(bin, 2);
        }
        System.out.print("\nhex view:");
        for (byte b : resultArray) {
            System.out.print(" " + Integer.toHexString(b));
        }
        System.out.print("\ntext view: " + new String(resultArray));
        file = new File("decoded.txt");
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(resultArray);
        } catch (IOException e) {
            System.out.println("ERROR");
        }
    }

    public static String fixByte(String bin) {
        int errorBit = 0;
        int b1 = Integer.parseInt(bin.substring(0, 1));
        int b2 = Integer.parseInt(bin.substring(1, 2));
        int b3 = Integer.parseInt(bin.substring(2, 3));
        int b4 = Integer.parseInt(bin.substring(3, 4));
        int b5 = Integer.parseInt(bin.substring(4, 5));
        int b6 = Integer.parseInt(bin.substring(5, 6));
        int b7 = Integer.parseInt(bin.substring(6, 7));
        int b8 = Integer.parseInt(bin.substring(7, 8));
        if (b1 != (b3 + b5 + b7) % 2) {
            errorBit++;
        }
        if (b2 != (b3 + b6 + b7) % 2) {
            errorBit += 2;
        }
        if (b4 != (b5 + b6 + b7) % 2) {
            errorBit += 4;
        }
        if (b8 != 0) {
            errorBit += 8;
        }
        return bin.substring(0, errorBit - 1)
                + (Character.getNumericValue(bin.charAt(errorBit - 1)) + 1) % 2
                + bin.substring(errorBit);
    }
}