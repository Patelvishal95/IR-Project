
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Chart;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.charts.AxisCrosses;
import org.apache.poi.ss.usermodel.charts.AxisPosition;
import org.apache.poi.ss.usermodel.charts.ChartAxis;
import org.apache.poi.ss.usermodel.charts.ChartDataSource;
import org.apache.poi.ss.usermodel.charts.ChartLegend;
import org.apache.poi.ss.usermodel.charts.DataSources;
import org.apache.poi.ss.usermodel.charts.LegendPosition;
import org.apache.poi.ss.usermodel.charts.LineChartData;
import org.apache.poi.ss.usermodel.charts.LineChartSeries;
import org.apache.poi.ss.usermodel.charts.ValueAxis;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLineSer;


public class PrecisionRecall {

    //file f is the file whose evaluation you need to do
    ArrayList[] measures = new ArrayList[65];
    double[] AP,Rank;
    //used to write in excel
     private static String[] columns = {"Rank", "File", "Precision", "Recall"};
    
    
    
    public static void main(String[] args) {
        File f = new File("../../Pratik Devikar/IR-Project/Task1/BM-25_Results");//First run
        
        //add all such file with paths like this
        PrecisionRecall obj = new PrecisionRecall();
        obj.init();//need to be runned once
        //supply folder of evaluation here program will take care for all queries
        //Second argument is the format of the file of run
        obj.startevaluation(f, "BM25_scores_query_","");//reusable part
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task1/SQLM_Results"), "SQLM_scores_query_","");
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task1/TFIDF_Results"), "TFIDF_scores_query_","");

        //query enrichment run
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task2/BM25_Results_Query_Enrichment"), "BM25_scores_query_enriched_query_","");
        
        //lucene run
        obj.startevaluation(new File("../../SaranPrasad/output/lucene_indexer/RankedResults_cacm"), "queryID_","lucene");
        obj.startevaluation(new File("../../SaranPrasad/output/lucene_indexer/RankedResults_cacm_with_stopping"), "queryID_","lucenestopped");
        
        //stopping 2 runs
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task3/BM25_Results_Stopping"), "BM25_scores_stopping_query_","");
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Task3/SQLM_Results_Stopping"), "SQLM_scores_stopping_query_","");
        
        //extra credit
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Extra-Credit/BM25_results_spelling_corrected"), "BM25_scores_spelling_corrected_query_","");
        obj.startevaluation(new File("../../Pratik Devikar/IR-Project/Extra-Credit/BM25_results_spelling_error"), "BM25_scores_spelling_error_query_","");
        
    }
//tempflag is introduced because lucene query has same name format program will just overwrite to the same folder
    private void startevaluation(File toevaluate, String Filenameformat,String tempflag) {
        File[] listoffiles = toevaluate.listFiles();
        AP=new double[65];
        Rank = new double[65];
        for (File queryrun : listoffiles) {
            String name = (queryrun.getName());
            name = name.replace(Filenameformat, "");
            int number = Integer.valueOf(name.replaceAll(".txt", ""));
            Calculation(queryrun, number, Filenameformat,tempflag);
        }
        WriteToFile writer = new WriteToFile(new File("Evaluation/"+tempflag+Filenameformat+"/"+"MAPandMRR.txt"));
        writer.write("MAP is -> "+MAP()+"\n");
        writer.write("MRR is -> "+MAR()+"\n");
        writer.close();
    }

    private void init() {
        //intitialize the relevance measures here
        File measures = new File("Measures/");
        //this has list of all measure files load it to an arrayList
        File[] listmeasures = measures.listFiles();
        //first index is the query number of the relevance measures
        for (File fileinmeasure : listmeasures) {
            ReadFiletoarrayList(fileinmeasure);
        }
        //printmeasures();
    }

    private void ReadFiletoarrayList(File f) {

        ArrayList<String> toplace = new ArrayList<>();
        int pos = Integer.valueOf(f.getName().replaceAll(".txt", ""));

        BufferedReader b = null;
        try {
            b = new BufferedReader(new FileReader(f));
            String read = b.readLine();
            while (read != null) {
                toplace.add(read);
                read = b.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                b.close();
            } catch (IOException ex) {
                Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        measures[pos] = toplace;
    }

    private void Calculation(File queryrun, int querynumber, String Filenameformat,String tempflag) {
            if(measures[querynumber]==null){return;}
        double[] precision = new double[101];
        double[] recall = new double[101];
        int[] match = GetMatch(queryrun,querynumber);
        int numbermatch = 0;
        for(int i=1;i<=100;i++){
            if(match[i]==1){
                numbermatch++;
            }
            precision[i]=(double)numbermatch / (double) i;
            recall[i]= (double)numbermatch/(double)measures[querynumber].size();
        }
        //excel init
        // Create a Workbook
        XSSFWorkbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file

        /* CreationHelper helps us create instances for various things like DataFormat, 
           Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way */
        CreationHelper createHelper = workbook.getCreationHelper();

        // Create a Sheet
        Sheet sheet = workbook.createSheet("Analysis");
        
        DataFormat format = workbook.createDataFormat();
        // Create a Font for styling header cells
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        // Create a CellStyle with the font
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Create a Row
        Row headerRow = sheet.createRow(0);

        // Creating cells
        for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }
      
        
        
        
        //excel init ends
        //writing to this file
        File cretdir = new File("Evaluation/"+tempflag+Filenameformat);
        cretdir.mkdirs();
        WriteToFile writer = new WriteToFile(new File("Evaluation/"+tempflag+Filenameformat+"/"+Filenameformat+querynumber+"_"+"evaluation.txt"));
        writer.write("Ranking  DocID      Precision               Recall\n");
        try {
            BufferedReader b = new BufferedReader(new FileReader(queryrun));//this read is for getting file number
            String read = b.readLine();int i=1;int rowNum =1;
            while (read != null) {
                if(read.equalsIgnoreCase("")){break;}
                writer.write("  "+String.format("%03d", i)+"    "+read.split(" ")[2]+"  "+String.format("%1.20f", precision[i])+"  "+String.format("%1.20f", recall[i])+"\n");
                
                //inserting after this for excel
                 Row row = sheet.createRow(rowNum++);
                row.createCell(0)
                    .setCellValue(i);
                row.createCell(1)
                    .setCellValue(read.split(" ")[2]);
                row.createCell(2)
                    .setCellValue(precision[i]);
                row.createCell(3)
                    .setCellValue(recall[i]);
                
                //inserting completed for excel
                read=b.readLine();i++;
                if(i==101)break;
            }
        } catch (IOException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        }
        double map = AP(precision,match);
        double rank = Rank(match);
        AP[querynumber]=map;
        Rank[querynumber]=rank;
        writer.write("P@k = 5 -> "+precision[5]+"\n");
        writer.write("P@k = 20 -> "+precision[20]+"\n");
        writer.write("Average precision is -> "+map+"\n");
        writer.write("Reciprocal Rank is -> "+rank+"\n");
        writer.close();
        
        //Excel write file
         // Write the output to a file
        try{
            for(int k=0;k<4;k++){
                sheet.autoSizeColumn(k);
            }
            
            Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 5, 0, 15, 20);

        Chart chart = drawing.createChart(anchor);

        LineChartData data = chart.getChartDataFactory().createLineChartData();
           ChartLegend legend = chart.getOrCreateLegend();

    legend.setPosition(LegendPosition.TOP_RIGHT);
    legend.setOverlay(true);
        ChartAxis bottomAxis = chart.getChartAxisFactory().createCategoryAxis(AxisPosition.BOTTOM);
        ValueAxis leftAxis = chart.getChartAxisFactory().createValueAxis(AxisPosition.LEFT);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
//        bottomAxis.setMaximum(1.0);
//        bottomAxis.setMinimum(0.0);
        bottomAxis.setNumberFormat("0.0000");
        leftAxis.setMaximum(1.0);
        leftAxis.setMinimum(0.0);
        ChartDataSource<Number> x = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, 100, 2, 2));
        ChartDataSource<Number> y = DataSources.fromNumericCellRange(sheet, new CellRangeAddress(1, 100,3, 3));
        LineChartSeries series1 = data.addSeries(y, x);
        
        series1.setTitle("Precision vs recall\nPrecision on X-Axis\nRecall on y-Axis");

        chart.plot(data, bottomAxis, leftAxis);
        
//        XSSFChart xssfChart = (XSSFChart) chart;
//        CTPlotArea plotArea = xssfChart.getCTChart().getPlotArea();
//        plotArea.getLineChartArray()[0].getSmooth();
//        CTBoolean ctBool = CTBoolean.Factory.newInstance();
//        ctBool.setVal(false);
//        plotArea.getLineChartArray()[0].setSmooth(ctBool);
//        for (CTLineSer ser : plotArea.getLineChartArray()[0].getSerArray()) {
//            ser.setSmooth(ctBool);
//        }
        FileOutputStream fileOut = new FileOutputStream(new File("Evaluation/"+tempflag+Filenameformat+"/"+Filenameformat+querynumber+"_"+"evaluation.xlsx"));
        workbook.write(fileOut);
        fileOut.close();

        // Closing the workbook
        workbook.close();}
        catch (IOException ex) {
        Logger.getLogger(WriteToFile.class.getName()).log(Level.SEVERE, null, ex);
    }
    }

    private int[] GetMatch(File queryrun,int querypointer) {
        int[] toret = new int[101];
        try {
            BufferedReader b = new BufferedReader(new FileReader(queryrun));
            ArrayList<String> tocheck = measures[querypointer];
            String read = b.readLine();int i=1;
            while (read != null) {
                if(read.equalsIgnoreCase("")){read=b.readLine();toret[i]=0;continue;}
                toret[i] = findinmeasures(tocheck,(read.split(" ")[2]));
                read = b.readLine();i++;
                if(i==101){return toret;}
            }
        } catch (IOException ex) {
            Logger.getLogger(PrecisionRecall.class.getName()).log(Level.SEVERE, null, ex);
        }
        return toret;
    }

    private int findinmeasures(ArrayList<String> tocheck, String string) {
        for(String tomatch:tocheck){
            if(tomatch.equalsIgnoreCase(string)){
                return 1; 
            }
        }return 0;
    }

    private double Rank(int[] match) {
       for(int i=1;i<=100;i++) {
       if(match[i]==1){
           return 1/(double) i;
       }
       }
       return 0;
    }

    private double AP(double[] precision, int[] match) {
    double toret=0.0;int count=0;
        for(int i=1;i<=100;i++) {
        if(match[i]==1){
            count++;
            toret+=precision[i];
        }
    }
        return toret/(double)count;
    }

    private String MAP() {
        double toret=0.0;
        for(double toadd : AP){
            toret+=toadd;
        }
        
        return String.valueOf(toret/52.0);
    }

    private String MAR() {
        double toret=0.0;
        for(double toadd : Rank){
            toret+=toadd;
        }
        
        return String.valueOf(toret/52.0);
    }
    
}
