package valentecaio.mapquestapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by caio on 18/06/2017.
 */

public class AppFileManager {
    private String name;
    private Context context;

    private static String fileType = ".csv";
    private static String separator = "&&&";
    private static String point_prefix = "point_";
    private static String balade_prefix = "balade_";

    public AppFileManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String nameWithType() {
        String str = this.name;
        if (!this.name.contains(this.fileType)) {
            str += this.fileType;
        }
        return str;
    }

    private void write(String data) {
        try {
            String nameToWrite = nameWithType();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                    context.openFileOutput(nameToWrite, Context.MODE_PRIVATE));
                    //context.openFileOutput(nameToWrite, Activity.MODE_WORLD_READABLE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            Log.i("AFM", "WRITE filename: " + nameToWrite + ", content: " + data);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private boolean formatIsCSV(File file) {
        return file.getName().contains(fileType);
    }

    public String read() {
        String ret = "";
        try {
            String nameToRead = nameWithType();
            InputStream inputStream = context.openFileInput(nameToRead);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
                Log.i("AFM", "READ filename: " + nameToRead + ", content: " + ret);
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public Point readPoint(int id){
        // read csv file
        this.setName(point_prefix + id + fileType);
        String csv = read();

        // split read data
        String[] data = csv.split(separator);
        String name = data[1];
        String description = data[2];
        Double longitude = new Double(data[3]);
        Double latitude = new Double(data[4]);

        // transform string data in Point object
        Point p = new Point(id, latitude, longitude, name, description);
        for(int i=5; i<data.length; i++){
            p.addMedia(data[i]);
        }
        return p;
    }

    // read balade and load points and medias
    public Balade readBalade(int id){
        // read csv file
        this.setName(balade_prefix + id + fileType);
        String csv = read();

        // split read data
        String[] data = csv.split(separator);
        String name = data[1];
        String theme = data[2];

        // transform string data in Balade object
        Balade b = new Balade(id, name, theme);
        for(int i=3; i<data.length; i++){
            b.addPoint(readPoint(Integer.parseInt(data[i])));
        }
        return b;
    }

    public ArrayList<Balade> listDownloadedBalades(){
        ArrayList<Balade> balades = new ArrayList<>();
        File[] files = this.getFiles();
        for(File f: files){
            if(f.getName().contains(balade_prefix)){
                int start = balade_prefix.length();
                int end = f.getName().length() - fileType.length();
                int id = Integer.parseInt(f.getName().substring(start, end));
                balades.add(readBalade(id));
            }
        }
        return balades;
    }

    public void writePoint(Point p){
        String s = p.getId()
                + separator + p.getName()
                + separator + p.getDescription()
                + separator + p.getLongitude()
                + separator + p.getLatitude();
        ArrayList<String> medias = p.getMedias();
        for(String m: medias){
            s +=  separator + m;
        }

        this.setName(point_prefix + p.getId() + fileType);
        write(s);
    }

    private void writeBalade(Balade b){
        String s = b.getId()
                + separator + b.getName()
                + separator + b.getTheme();
        ArrayList<Point> points = b.getPoints();
        for(Point p: points){
            s +=  separator + p.getId();
        }

        this.setName(balade_prefix + b.getId() + fileType);
        write(s);
    }

    public void writeBaladeAndPoints(Balade b){
        writeBalade(b);

        for(Point p: b.getPoints()){
            writePoint(p);
        }
    }

    public File[] getFiles(){
        String path = this.context.getFilesDir().getAbsolutePath();
        Log.i("AFM", "getFiles Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        for(File f: files) {
            Log.i("GET_FILES", f.getAbsolutePath());
        }
        return files;
    }

    public File getFile(String filename){
        File[] files = getFiles();
        for(File f: files){
            if(f.getName().equals(filename)){
                return f;
            }
        }
        return null;
    }

    public ArrayList<String> readAll() {
        File[] files = this.getFiles();

        // read files
        ArrayList<String> results = new ArrayList<String>();
        for (File file : files) {
            if (formatIsCSV(file)) {
                this.name = (file.getName());
                results.add(read());
            }
        }
        return results;
    }

    public boolean deleteAllCSVData() {
        File[] files = this.getFiles();

        boolean deleted = true;
        for (File file : files) {
            if (formatIsCSV(file)) {
                Log.i("AFM", "DELETE filename: " + file.getName());
                deleted = file.delete() && deleted;
            }
        }
        return deleted;
    }

    public boolean deleteAll() {
        File[] files = this.getFiles();

        boolean deleted = true;
        for (File file : files) {
            String db_extension = ".db";
            if (!file.getName().contains(db_extension)) {
                Log.i("AFM", "DELETE filename: " + file.getName());
                deleted = file.delete() && deleted;
            }
        }
        return deleted;
    }

    public boolean deleteFile() {
        File[] files = this.getFiles();

        boolean deleted = true;
        for (File file : files) {
            String nameToDelete = nameWithType();
            if (nameToDelete.equals(file.getName()) && formatIsCSV(file)) {
                Log.i("AFM", "DELETE filename: " + file.getName());
                deleted = file.delete();
            }
        }
        return deleted;
    }
}