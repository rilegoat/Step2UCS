import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class app extends Application {

    String version = "v0.2";

    Label SMPrompt = new Label("Load chart file (.sm):");
    Label UCSPrompt = new Label("Import sample file (.ucs):");
    Label SMPath = new Label("C:\\");
    Label UCSPath = new Label("C:\\");
    Label UCSFilename = new Label("Sample file name: [file not loaded]");
    Label UCSFileOffset = new Label("Sample file offset: [file not loaded]");
    Label softwareLabel = new Label("Title");
    Pane SMSpacer = new Pane();
    Pane UCSSpacer = new Pane();
    Button importUCSButton = new Button("Open");
    Button importSMButton = new Button("Open");
    Button generateButton = new Button("Convert .sm to .ucs");

    boolean chartStarted = false;       // Have we started recording the chart yet?
    boolean isDouble = true;            // Is this chart a double?
    float bpm;                          // Store the bpm for each timing segment
    float importDelay;                  // Store the delay the chart needs at the start
    boolean[] isHeld = new boolean[10]; // Index for each note lane---is it a hold note?
    File inputSMFile;
    File outputUCSFile;
    String chartType = "";              // Single, Double, D-Performance (Coop)

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.importUCSButton.setOnAction(new importSampleHandler());    // Trigger on clicking sample "Open" button
        this.generateButton.setOnAction(new generateButtonHandler());   // Trigger on clicking "Convert .sm to .ucs" button
        this.importSMButton.setOnAction(new importSMHandler());         // Trigger on clicking .sm "Open" button

        // Load fonts
        Font aspekta100 = Font.loadFont(new File("./resources/fonts/Aspekta-100.ttf").toURI().toString(), 12);
        Font aspekta250 = Font.loadFont(new File("./resources/fonts/Aspekta-250.ttf").toURI().toString(), 12);
        Font aspekta400 = Font.loadFont(new File("./resources/fonts/Aspekta-400.ttf").toURI().toString(), 12);
        Font aspekta600 = Font.loadFont(new File("./resources/fonts/Aspekta-600.ttf").toURI().toString(), 12);
        Font aspekta800 = Font.loadFont(new File("./resources/fonts/Aspekta-800.ttf").toURI().toString(), 20);
        Font aspekta1000 = Font.loadFont(new File("./resources/fonts/Aspekta-1000.ttf").toURI().toString(), 20);

        // Area in scene for the sample .ucs file import
        HBox importSampleRow = new HBox(UCSPrompt, UCSSpacer, importUCSButton);
        HBox.setHgrow(UCSSpacer, Priority.ALWAYS);
        importSampleRow.setAlignment(Pos.CENTER);
        UCSPrompt.setFont(aspekta400);
        importUCSButton.setFont(aspekta600);
        VBox importSampleVBox = new VBox(importSampleRow, UCSPath);
        UCSPath.setFont(aspekta250);

        // Area in scene for the .sm file import
        HBox importSMRow = new HBox(SMPrompt, SMSpacer, importSMButton);
        HBox.setHgrow(SMSpacer, Priority.ALWAYS);
        importSMRow.setAlignment(Pos.CENTER);
        SMPrompt.setFont(aspekta400);
        importSMButton.setFont(aspekta600);
        VBox importSMVBox = new VBox(importSMRow, SMPath);
        SMPath.setFont(aspekta250);

        // Holds the two labels that show chart name & initial delay
        VBox chartDataVBox = new VBox(UCSFilename, UCSFileOffset);
        chartDataVBox.setAlignment(Pos.CENTER);
        UCSFilename.setFont(aspekta400);
        UCSFileOffset.setFont(aspekta400);

        // Update the title near the top of the screen
        softwareLabel.setText("Step2UCS [" + version + "]");
        softwareLabel.setFont(aspekta800);

        // Entire scene in one VBox! Put everything in here
        VBox entireScene = new VBox(softwareLabel, importSampleVBox, importSMVBox, chartDataVBox, generateButton);
        generateButton.setFont(aspekta800);
        entireScene.setAlignment(Pos.CENTER);
        entireScene.setPadding(new Insets(20));
        entireScene.setSpacing(20);

        // Make the window using the stage
        Scene main = new Scene(entireScene);
        primaryStage.setScene(main);
        primaryStage.setMinWidth(420);
        primaryStage.setMinHeight(360);
        primaryStage.setTitle("Step2UCS [" + version + "]");
        primaryStage.show();
    }

    public static void main(String[] args) { Application.launch(args); }

    private static void convertBeat(Scanner inFile, FileWriter outWriter, String curLine, float bpm, boolean isDoub, boolean[] isHeldArr, boolean initialD, float importD) {
        Vector<String> beatVector = new Vector<>();
        int beatSplit;
        beatVector.add(curLine);
        while(inFile.hasNextLine()){
            String inFileLine = inFile.nextLine();
            if (inFileLine.equals(",") || inFileLine.equals(";")) { break; } // come back to this
            beatVector.add(inFileLine);
        }
        beatSplit = beatVector.size() / 4;
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
                    UCSFilename.setText("Sample file name: " + selectedFile.getName());
                    UCSFileOffset.setText("Sample file offset: " + importDelay + "ms");
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
        UCSPath.setText(outputUCSFile.getAbsolutePath());
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