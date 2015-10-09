package com.xamarin.jenkins.wrenchaggregator;

import hudson.Extension;
import hudson.matrix.Combination;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.matrix.MatrixRun;
import hudson.plugins.git.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TransientProjectActionFactory;
import hudson.util.RunList;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.jvnet.hudson.plugins.groovypostbuild.GroovyPostbuildSummaryAction;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class Summary extends InvisibleAction {

    final private AbstractProject<?, ?> project;
    final private Combination Axis;

    public Summary(@SuppressWarnings("rawtypes") AbstractProject project, Combination Axis) {
        this.project = project;
        this.Axis = Axis;
    }

    public RunList<?> getBuilds() {
        return project.getBuilds().limit(30);
    }
    
    public String getSha1(AbstractBuild<?, ?> target) {
        return ((GitSCM)(project.getScm())).getBuildData(target).getLastBuiltRevision().getSha1String();
    }
    
    public String getRepoUrl() {
        return ((GitSCM)(project.getScm())).getBrowser().getRepoUrl();
    }
    
    public Boolean getIsMatrix() {
        return (project instanceof MatrixProject && Axis == null);
    }
    
    public Boolean getIsAxis() {
        return (Axis != null);
    }
    
    public String getAxisLabel() {
        return (Axis == null) ? "" : Axis.toString();
    }
    
    public ArrayList<String> getStepHeaders() {
        int mostSeen = 0;
        ArrayList<String> results = new ArrayList<String>();
        for(Run target: getBuilds()) {
            String rawStatus = ((GroovyPostbuildSummaryAction)(target.getActions(GroovyPostbuildSummaryAction.class).toArray()[0])).getText();
            rawStatus = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + rawStatus.substring(rawStatus.indexOf("</h1>") + 5);
            try {
                StringBuilder result = new StringBuilder();
                XPath xpath = XPathFactory.newInstance().newXPath();
                InputSource inputSource = new InputSource( new StringReader( rawStatus ) );
                NodeList nodes = (NodeList) xpath.evaluate("/table/tr/td[position()=1]", inputSource, XPathConstants.NODESET);
                if(nodes.getLength() > mostSeen) {
                    results = new ArrayList<String>();
                    mostSeen = nodes.getLength();
                    for(int i=0; i < nodes.getLength(); i++) {
                        results.add(nodes.item(i).getTextContent());
                    }
                }
            } catch(Exception e) {
                return new ArrayList<String>();
            }
        }
        return results;
    }
    
    public ArrayList<String> getMatrixVariables(MatrixBuild target) {
        ArrayList<String> results = new ArrayList<String>();
        List<MatrixRun> rawResults = target.getExactRuns();
        for(MatrixRun myRun: rawResults) {
            results.add(myRun.getBuildVariables().values().iterator().next());
        }
        return results;
    }
    
    public String getMatrixSummary(MatrixBuild target) {
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < target.getExactRuns().size() - 1; i++) {
            result.append("<td style=\"border-spacing: 0px; border: 1px solid black;\"><a href=\"");
            result.append(target.getExactRuns().get(i).getAbsoluteUrl());
            result.append("\">");
            result.append(target.getExactRuns().get(i).getBuildVariables().values().iterator().next());
            result.append("</a></td>");
            result.append(getSummary(target.getExactRuns().get(i)));
            result.append("</tr><tr>");
        }
        result.append("<td style=\"border-spacing: 0px; border: 1px solid black;\"><a href=\"");
        result.append(target.getExactRuns().get(target.getExactRuns().size() - 1).getAbsoluteUrl());
        result.append("\">");
        result.append(target.getExactRuns().get(target.getExactRuns().size() - 1).getBuildVariables().values().iterator().next());
        result.append("</a></td>");
        result.append(getSummary(target.getExactRuns().get(target.getExactRuns().size() - 1)));
        return result.toString();
    }
    
    public String getSummary(AbstractBuild<?, ?> target) {
        String rawStatus = ((GroovyPostbuildSummaryAction)(target.getActions(GroovyPostbuildSummaryAction.class).toArray()[0])).getText();
        rawStatus = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + rawStatus.substring(rawStatus.indexOf("</h1>") + 5);
        try {
            StringBuilder result = new StringBuilder();
            XPath xpath = XPathFactory.newInstance().newXPath();
            InputSource inputSource = new InputSource( new StringReader( rawStatus ) );
            NodeList nodes = (NodeList) xpath.evaluate("/table/tr/td[position()=4]", inputSource, XPathConstants.NODESET);
            for(int i=0; i < nodes.getLength(); i++) {
                String color = nodes.item(i).getAttributes().getNamedItem("bgcolor").getNodeValue();
                if(color.equals("#ff0000"))
                    result.append("<td style=\"background-color: #ff0000; border-spacing: 0px; border: 1px solid black;\">✗</td>");
                else if(color.equals("#00ff7f"))
                    result.append("<td style=\"background-color: #00ff7f; border-spacing: 0px; border: 1px solid black;\">✓</td>");
                else if(color.equals("#ffa500"))
                    result.append("<td style=\"background-color: #ffa500; border-spacing: 0px; border: 1px solid black;\">☹</td>");
                else if(color.equals("#000000"))
                    result.append("<td style=\"background-color: #000000; color: #ffffff; border-spacing: 0px; border: 1px solid black;\">⌛</td>");
                else
                    result.append("<td style=\"background-color: #d3d3d3; border-spacing: 0px; border: 1px solid black;\">?</td>");
            }
            return result.toString();
        } catch(Exception e) {
            return "<td style=\"background-color: #ff0000;\">ERROR</td>";
        }
    }
    
    public String getColor(AbstractBuild<?, ?> target) {
        if(target.getResult() != null && target.getResult().isCompleteBuild()) {
            if(target.getResult().equals(Result.SUCCESS))
                    return "00ff7f";
            if(target.getResult().equals(Result.UNSTABLE))
                    return "ffa500";
            if(target.getResult().equals(Result.FAILURE))
                    return "ff0000";
            return "d3d3d3";
        }
        return "ffffff";
    }

    @Override
    public String toString() {
        return "Wrench aggregation for " + project.toString();
    }

    @Extension
    public static class SummaryFactory extends TransientProjectActionFactory {

        /**
         * For matrix projects parameter actions are attached to the MatrixProject
         */
        @Override
        public Collection<? extends Action> createFor(
                @SuppressWarnings("rawtypes") AbstractProject target
        ) {
            Combination Axis;
            
            Axis = (target instanceof MatrixConfiguration) ? ((MatrixConfiguration) target).getCombination() : null;
            
            return Arrays.asList(new Summary(target, Axis));
        }
    }
}
