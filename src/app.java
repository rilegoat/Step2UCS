import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class app extends Application {

    Button importSampleButton = new Button("Open");
    Button importSMButton = new Button("Open");
    Button generateUCSButton = new Button("Convert to .UCS");

    Label SMPrompt = new Label("Load chart file (.sm):");
    Label samplePrompt = new Label("Import sample file (.ucs):");
    Label SMPath = new Label("C:\\");
    Label samplePath = new Label("C:\\");
    Label sampleFilename = new Label("Sample file name: [file not loaded]");
    Label sampleFileOffset = new Label("Sample file offset: [file not loaded]");
    Label softwareLabel = new Label("Title");

    boolean chartStarted = false; // Have we started recording the chart yet?
    boolean isDouble = true; // is this chart a double?
    float bpm = 100;
    float importDelay = 0;
    boolean[] isHeld = new boolean[10];
    File inputSMFile;
    File outputUCSFile;
    String chartType = "";

    public static void main(String[] args) { Application.launch(args); }

    private static void convertBeat(Scanner inFile, FileWriter outWriter, String curLine, float bpm, boolean isDoub, boolean[] isHeldArr, boolean initialD, float importD) {
        Vector<String> beatVector = new Vector<>();
        int beatSplit;
        beatVector.add(curLine);
//        System.out.println(beatVector.lastElement());
        while(inFile.hasNextLine()){
            String inFileLine = inFile.nextLine();
            if (inFileLine.equals(",") || inFileLine.equals(";")) { break; } // come back to this
            beatVector.add(inFileLine);
        }
//        System.out.println("Rows in this measure: " + beatVector.size());
        beatSplit = beatVector.size() / 4;
//        System.out.println("Splits per beat: " + (beatVector.size() / 4));
        try {
            outWriter.write(":BPM=" + String.valueOf(bpm) + "\n");
            outWriter.write(":Delay=" + ((initialD) ? importD : 0) + "\n");
            outWriter.write(":Beat=4\n");
            outWriter.write(":Split=" + beatSplit + "\n");
            if (initialD) {
                initialD = false;
            }
            beatVector.forEach((n) -> {
                String addLine = "";
                if (isDoub) {
                    for (int i = 0; i < ((isDoub) ? 10 : 5); i++) {
                        if (n.charAt(i) == '0') {
                            if (isHeldArr[i] == false) {
                                addLine += ".";
                            } else if (isHeldArr[i] == true){
                                addLine += "H";
                            }
                        }
                        if (n.charAt(i) == '1') {
                            addLine += "X";
                        }
                        if (n.charAt(i) == '2') {
                            addLine += "M";
                            isHeldArr[i] = true;
                        }
                        if (n.charAt(i) == '3') {
                            addLine += "W";
                            isHeldArr[i] = false;
                        }
                    }
                }
                try {
                    outWriter.write(addLine + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.importSampleButton.setOnAction(new importSampleHandler());
        this.generateUCSButton.setOnAction(new generateButtonHandler());
        this.importSMButton.setOnAction(new importSMHandler());

        HBox importSMRow = new HBox(SMPrompt, importSMButton);
        VBox importSMVBox = new VBox(importSMRow, SMPath);
        HBox importSampleRow = new HBox(samplePrompt, importSampleButton);
        VBox importSampleVBox = new VBox(importSampleRow, samplePath);
        VBox chartDataVBox = new VBox(sampleFilename, sampleFileOffset);
        VBox entireScene = new VBox(softwareLabel, importSampleVBox, importSMVBox, chartDataVBox, generateUCSButton);

        entireScene.setSpacing(30);
        Scene main = new Scene(entireScene);
        primaryStage.setScene(main);
        primaryStage.setTitle(".SSC to .UCS Converter");
        primaryStage.show();
    }

    private class importSampleHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return "Sample UCS file (*.ucs)";
                }

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    } else {
                        return f.getName().toLowerCase().endsWith(".ucs");
                    }
                }
            });
            fileChooser.setAcceptAllFileFilterUsed(false);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                storeUCSPath(selectedFile);
                storeUCSMetadata(selectedFile);
            }
        }
    }

    private void storeUCSMetadata(File selectedFile) {
        File t = new File(selectedFile.toURI());
        try {
            Scanner scanner = new Scanner(t);
            while (scanner.hasNextLine()){
                String inLine = scanner.nextLine();
                if (inLine.contains(":Delay=")){
                    importDelay = Float.parseFloat(inLine.substring(inLine.indexOf("=") + 1));
                    System.out.println(importDelay);
                    sampleFilename.setText("Sample file name: " + selectedFile.getName());
                    sampleFileOffset.setText("Sample file offset: " + importDelay + "ms");
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void storeUCSPath(File selectedFile) {
        outputUCSFile = selectedFile;
        System.out.println(selectedFile.getAbsolutePath());
        samplePath.setText(outputUCSFile.getAbsolutePath());
    }

    private class importSMHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.addChoosableFileFilter(new FileFilter() {
                @Override
                public String getDescription() {
                    return "StepMania SM file (*.sm)";
                }

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    } else {
                        return f.getName().toLowerCase().endsWith(".sm");
                    }
                }
            });
            fileChooser.setAcceptAllFileFilterUsed(false);
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                storeSMPath(selectedFile);
            }
        }
    }

    private void storeSMPath(File selectedFile) {
        inputSMFile = selectedFile;
        System.out.println(selectedFile.getAbsolutePath());
        SMPath.setText(inputSMFile.getAbsolutePath());
    }

    private class generateButtonHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            System.out.println("Printing input file:");
            boolean initialDelay = true;
            try {
                Scanner inFile = new Scanner(inputSMFile); // read input file using inFile
                FileWriter outWrite = new FileWriter(outputUCSFile);
                try {
                    PrintWriter writer = new PrintWriter(outputUCSFile);
                    writer.print("");
                    writer.flush();
                    writer.close();
                } catch (Exception e) {
                    System.out.println("Error resetting chart file.");
                }
                outWrite.write(":Format=1\n");
                boolean writeType = true;
                // begin parsing input file
                while (inFile.hasNextLine()) {
                    String inFileLine = inFile.nextLine();
                    if (chartStarted) {
                        if (inFileLine.length() == 5 || inFileLine.length() == 10) { convertBeat(inFile, outWrite, inFileLine, bpm, isDouble, isHeld, initialDelay, importDelay); }
                        initialDelay = false;
                    }
                    if (inFileLine.contains("0.000=")) {
                        bpm = Float.parseFloat(inFileLine.substring(inFileLine.indexOf("0.000=") + 6, inFileLine.indexOf("0.000=") + 13));
                    }
                    if (inFileLine.contains("pump-single") && writeType) {
                        chartType = "Single";
                        isDouble = false;
                        outWrite.write(":Mode=" + chartType + "\n");
                        writeType = false;
                    } else if (inFileLine.contains("pump-double") && writeType) {
                        chartType = "Double";
                        isDouble = true;
                        outWrite.write(":Mode=" + chartType + "\n");
                        writeType = false;
                    } else if (inFileLine.contains("pump-couple") && writeType) {
                        chartType = "D-Performance";
                        isDouble = true;
                        outWrite.write(":Mode=" + chartType + "\n");
                        writeType = false;
                    }
                    if (inFileLine.contains("0,0,0,0,0:")) {
                        chartStarted = true;
                        System.out.println("--- CHART READ STARTED ---");
                    }
                }
                outWrite.close();
                inFile.close();
            } catch (IOException e) {
                System.out.println("An error occurred (input file).");
                e.printStackTrace();
            }
        }
    }
}