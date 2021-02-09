package soundsenderinator5;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
 
public class SoundSenderInator5 extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
    
    //set window title 
    primaryStage.setTitle("sounderer");
    
    //maek a 4x4 grid    
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    //spacing between four cells
    grid.setHgap(100);
    grid.setVgap(100);
    //space between grid and top, botton, left and right
    grid.setPadding(new Insets(25, 25, 25, 25));
    
    //add a text field
    TextField txt = new TextField();
    txt.setText(readIPFile());

    
    //add a stop button
    Button btn1 = new Button();
    btn1.setText("stop");
    //when button is preedes, sound.stop() is called which kills the thread and releases the line
    btn1.setOnAction(a->Soundd.stop());
    
    //add a start button
    Button btn = new Button();
    btn.setText("start");
    btn.setOnAction(a -> {
        //check if soundd.IPstr field is set and is a valid IP and sdard if valid else show "enter valid IP"
        Soundd.setIP(txt.getText().trim());
        if(Soundd.isset()&&validate(txt.getText().trim())) Soundd.sdard();
        else txt.setText("enter valid IP");
    });
    
    //enter IP button    
    Button btn2 = new Button();
    btn2.setText("set ip");
    btn2.setOnAction(a-> {
        //check if Ip is valid and update soundd.IPstr if valid else show "enrer valid IP" in field
        if(validate(txt.getText().trim())){ Soundd.setIP(txt.getText().trim());addIP2File(txt.getText());}
        else txt.setText("enrer valid IP");
    });
    
    Button btn3 = new Button();
    btn3.setText("multicast");
    btn3.setOnAction(a-> {
//     txt.setText("224.0.0.3");
     Soundd.setIP("224.0.0.3");
     Soundd.sdard();
    });
    
    //add all button and text field to the grid
    grid.add(btn,0,0,2,1);
    grid.add(btn1,0,1);
    grid.add(btn2,1,0);
    grid.add(txt, 1, 1);
    grid.add(btn3, 2, 1);
    //add the grid to he scene with size and add scene to the stage   
    primaryStage.setScene(new Scene(grid, 500, 500));
    //exit jvm when red x is clicked
    primaryStage.setOnCloseRequest(a->{Soundd.stop();System.exit(0);});
    primaryStage.setResizable(false);
    primaryStage.show();
    }
    
    //validate IP
    public static boolean validate(final String ip) {
    String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";
    return ip.matches(PATTERN);
}
    public static void addIP2File(String a){
        try (FileWriter file = new FileWriter(new File(System.getProperty("user.dir").concat("\\ip.tpz")))) {
            file.write(a.trim());
            file.close();
        }catch(Exception e){System.out.println("errir in writing ip to file "+e.toString());}
    }
    
    public static String readIPFile(){
        String s=null;
        try (BufferedReader file =new BufferedReader( new FileReader(new File(System.getProperty("user.dir").concat("\\ip.tpz")))) ) {
            s= file.readLine().trim();
            file.close();
        }catch(Exception e){System.out.println("errir in writing ip to file "+e.toString());}
        return s;
    }
}
