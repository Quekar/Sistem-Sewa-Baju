module com.mycompany.sewabaju {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.sql;

    requires mysql.connector.j;
    
    requires jbcrypt;
    
    requires org.apache.commons.io;
    
    requires org.apache.poi.ooxml;
    
    opens com.mycompany.sewabaju to javafx.fxml;
    opens com.mycompany.sewabaju.controllers to javafx.fxml;
    opens com.mycompany.sewabaju.controllers.admin to javafx.fxml;
    opens com.mycompany.sewabaju.controllers.pelanggan to javafx.fxml;
    
    opens com.mycompany.sewabaju.models to javafx.base;
    
    exports com.mycompany.sewabaju;
    exports com.mycompany.sewabaju.controllers;
    exports com.mycompany.sewabaju.controllers.admin;
    exports com.mycompany.sewabaju.controllers.pelanggan;
    exports com.mycompany.sewabaju.models;
    exports com.mycompany.sewabaju.models.enums;
}