package org.example;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;

public class MainView extends VBox {

   // private Button stepButton; Entfernt, da redundant (Toolbar)

    public static final int EDITING = 0;
    public static final int  SIMULATING = 1;


    private InfoBar infoBar;
    private Canvas canvas;

    private Affine affine;

    private Simulation simulation;
    private Simulation initialSimulation;

    private int applicationState = EDITING;

    // Variable Draw und Erase; Draw == 1

    private int drawMode = Simulation.ALIVE;

    public MainView() {

     // Entfernt, da Stepbutton nun in der Klasse Toolbar ist
     /*   this.stepButton = new Button("step");
        this.stepButton.setOnAction(actionEvent -> {
            simulation.step();
            draw();
        }); */

        this.canvas = new Canvas(400, 400);
        this.canvas.setOnMousePressed(this::handleDraw);
        this.canvas.setOnMouseDragged(this::handleDraw);
        this.canvas.setOnMouseMoved(this::handleMoved);

        this.setOnKeyPressed(this::onKeyPressed);

        Toolbar toolbar = new Toolbar(this);

        this.infoBar = new InfoBar();
        this.infoBar.setDrawMode(this.drawMode);
        this.infoBar.setCursorPosition(0, 0);

        Pane spacer = new Pane();
        spacer.setMinSize(0, 0);
        spacer.setMaxSize(Double.MAX_VALUE,Double.MAX_VALUE);
        VBox.setVgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(toolbar, this.canvas, spacer, infoBar);

        this.affine = new Affine();
        this.affine.appendScale(400/10f, 400/10f);

        // Änderung 23.10.21 this.Simulation --> this.initialSimulation
        // Die simulierte Simulation wird dann kopiert

        this.initialSimulation = new Simulation(10, 10);
        this.simulation = Simulation.copy(this.initialSimulation);

       // simulation.setAlive(2,2);
       // simulation.setAlive(3,2);
       // simulation.setAlive(4,2);

       // simulation.setAlive(5,5);
       // simulation.setAlive(5,6);
       // simulation.setAlive(6,5);
       // simulation.setAlive(6,6);
       // simulation.setAlive(4,5);
    }

    private void handleMoved(MouseEvent mouseEvent) {

        Point2D simCoord = this.getSimulationCoordinates(mouseEvent);

        this.infoBar.setCursorPosition((int) simCoord.getX(), (int) simCoord.getY());

    }

    // Fallunterscheidung Draw und Erase

    private void onKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.D){
            this.drawMode = Simulation.ALIVE;
            System.out.println("Draw mode");
        }else if(keyEvent.getCode() == KeyCode.E){
            this.drawMode = Simulation.DEAD;
            System.out.println("Erase mode");
        }
    }

    private void handleDraw(MouseEvent event) {

        // 23.10.21 Um korrektes Verhalten bei Step --> Draw zu gewährleisten

        if(this.applicationState == SIMULATING){
            return;
        }

        Point2D simCoord = this.getSimulationCoordinates(event);

        int simX = (int) simCoord.getX();
        int simY = (int) simCoord.getY();

        System.out.println(simX + "," + simY);

        this.initialSimulation.setState(simX, simY, drawMode);
        draw();


       /* try {
            Point2D simCoord = this.affine.inverseTransform(mouseX, mouseY);

            int simX = (int) simCoord.getX();
            int simY = (int) simCoord.getY();

            System.out.println(simX + ", " + simY);

            // DrawMode für Board

            this.simulation.setState(simX, simY, drawMode);
            draw();

        } catch (NonInvertibleTransformException e) {
            System.out.println("Could not invert transform");
        }*/
    }

    private Point2D getSimulationCoordinates(MouseEvent event) {
        double mouseX = event.getX();
        double mouseY = event.getY();


        try {
            Point2D simCoord = this.affine.inverseTransform(mouseX, mouseY);
            return simCoord;
        } catch (NonInvertibleTransformException e) {
            // System.out.println("Could not invert transform");
        } throw new RuntimeException("Non invertable transorm");
    }


    public void draw() {
        GraphicsContext g = this.canvas.getGraphicsContext2D();
        g.setTransform(this.affine);

        g.setFill(Color.LIGHTGRAY);
        g.fillRect(0, 0, 400, 400);

        // eingefuegt am 23.10.2021 (YT #6A)

        if(this.applicationState == EDITING){
            drawSimulation(this.initialSimulation);
        }else{
            drawSimulation(this.simulation);
        }
       /* g.setFill(Color.BLACK);
        for (int x = 0; x < this.simulation.width; x++) {
            for (int y = 0; y < this.simulation.height; y++) {

                if (this.simulation.getState(x, y) == Simulation.ALIVE) {
                    g.fillRect(x, y, 1, 1);

                }

            }
        } */

        g.setStroke(Color.GRAY);
        g.setLineWidth(0.05);
        for (int x = 0; x <= this.simulation.width; x++) {
            g.strokeLine(x, 0, x, 10);
        }

        for (int y = 0; y <= this.simulation.height; y++) {
            g.strokeLine(0, y, 10, y);

        }
    }

    // eingefuegt am 23.10.2021 (YT #6A)

    private void drawSimulation(Simulation simulationToDraw){
        GraphicsContext g = this.canvas.getGraphicsContext2D();
        g.setFill(Color.BLACK);
        for (int x = 0; x < simulationToDraw.width; x++) {
            for (int y = 0; y < simulationToDraw.height; y++) {

                if (simulationToDraw.getState(x, y) == Simulation.ALIVE) {
                    g.fillRect(x, y, 1, 1);

                }

            }
        }
    }

    public Simulation getSimulation() {
        return this.simulation;
    }

    public void setDrawMode(int newDrawMode) {

        this.drawMode = newDrawMode;
        this.infoBar.setDrawMode(newDrawMode);
    }

    public void setApplicationState(int applicationState) {
        if(applicationState == this.applicationState){
            return;
        }

        if(applicationState == SIMULATING) {
            this.simulation = Simulation.copy(this.initialSimulation);
        }

        this.applicationState = applicationState;

        System.out.println("Application State: " + this.applicationState);
    }
}
