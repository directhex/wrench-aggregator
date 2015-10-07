package com.xamarin.jenkins.wrenchaggregator;

import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.scm.*;
import hudson.plugins.git.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.TransientProjectActionFactory;
import hudson.util.RunList;
import java.util.Arrays;
import java.util.Collection;

public class Summary extends InvisibleAction {

    final private AbstractProject<?, ?> project;

    public Summary(@SuppressWarnings("rawtypes") AbstractProject project) {

        this.project = project;
    }

    public RunList<?> getBuilds() {

        return project.getBuilds();
    }
    
    public String getSha1(AbstractBuild<?, ?> target) {
        return ((GitSCM)(project.getScm())).getBuildData(target).getLastBuiltRevision().getSha1String();
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

            if (target instanceof MatrixConfiguration) {

                target = ((MatrixConfiguration) target).getParent();
            }
            
            return Arrays.asList(new Summary(target));
        }
    }
}
