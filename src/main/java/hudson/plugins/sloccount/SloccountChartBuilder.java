package hudson.plugins.sloccount;

import hudson.plugins.sloccount.model.SloccountLanguageStatistics;
import hudson.util.ChartUtil.NumberOnlyBuildLabel;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;

import java.awt.Color;
import java.io.Serializable;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author lordofthepigs
 */
public class SloccountChartBuilder implements Serializable {

    private SloccountChartBuilder(){
    }

    public static JFreeChart buildChart(SloccountBuildAction action){
       
        String strLines = Messages.Sloccount_ReportSummary_Lines();

        JFreeChart chart = ChartFactory.createStackedAreaChart(null, null,
                strLines, buildDataset(action), PlotOrientation.VERTICAL,
                true, false, true);

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        SloccountAreaRenderer renderer = new SloccountAreaRenderer(action.getUrlName());
        plot.setRenderer(renderer);

        return chart;
    }

    private static CategoryDataset buildDataset(SloccountBuildAction lastAction){
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

        SloccountBuildAction action = lastAction;
        do{
            SloccountResult result = action.getResult();
            if(result != null){
                NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel(action.getBuild());

                for(SloccountLanguageStatistics l : result.getStatistics()){
                    builder.add(l.getLineCount(), l.getName(), buildLabel);
                }
            }

            action = action.getPreviousAction();
        }while(action != null);

        return builder.build();
    }
    
    public static JFreeChart buildChartDelta(SloccountBuildAction action){
        
        String strLinesDelta = Messages.Sloccount_ReportSummary_Lines()
                + " " + Messages.Sloccount_Trend_Delta();

        JFreeChart chart = ChartFactory.createStackedAreaChart(null, null,
                strLinesDelta, buildDatasetDelta(action), PlotOrientation.VERTICAL,
                true, false, true);

        chart.setBackgroundPaint(Color.white);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setForegroundAlpha(0.8f);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // crop extra space around the graph
        plot.setInsets(new RectangleInsets(0, 0, 0, 5.0));

        SloccountAreaRenderer renderer = new SloccountAreaRenderer(action.getUrlName());
        plot.setRenderer(renderer);

        return chart;
    }

    private static CategoryDataset buildDatasetDelta(SloccountBuildAction lastAction){
        DataSetBuilder<String, NumberOnlyBuildLabel> builder = new DataSetBuilder<String, NumberOnlyBuildLabel>();

        SloccountBuildAction action = lastAction;
        
        while(action != null){
            SloccountBuildAction previousAction = action.getPreviousAction();
            SloccountResult result = action.getResult();
            List<SloccountLanguageStatistics> previousStatistics = null;
            
            if(result != null){
                NumberOnlyBuildLabel buildLabel = new NumberOnlyBuildLabel(action.getBuild());
                
                if(previousAction != null && previousAction.getResult() != null){
                    previousStatistics = previousAction.getResult().getStatistics();
                } else {
                    // This will produce zero delta for the first build
                    previousStatistics = result.getStatistics();
                }

                for(SloccountLanguageStatistics current : result.getStatistics()){
                    SloccountLanguageStatistics previous = ReportSummary.getLanguage(previousStatistics, current.getName());
                    
                    builder.add(current.getLineCount() - previous.getLineCount(),
                            current.getName(), buildLabel);
                }
            }

            action = previousAction;
        };

        return builder.build();
    }
}
