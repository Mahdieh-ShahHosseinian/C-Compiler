package Parser;

import Compiler.Compiler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Farkhondeh Arzi
 * @author Mahdieh ShahHosseinian
 * Date: 6/19/2021 - Semester 4
 */

public class ParserTree extends Application {

    private final TreeView<String> parserTree = new TreeView<>();

    {
        parserTree.setMinSize(500, 500);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Compiler compiler = new Compiler();
        compiler.compile();
        parserTree.setRoot(compiler.getParserTreeRoot());

        Pane pane = new Pane();
        pane.getChildren().add(parserTree);
        Scene scene = new Scene(pane, 500, 500);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }
}
