module security{
    requires java.desktop;
    requires miglayout;
    requires com.google.gson;
    requires java.prefs;
    requires com.google.common;
    requires java.sql;
    requires image;
    opens com.udacity.catpoint.security.data to com.google.gson;
}