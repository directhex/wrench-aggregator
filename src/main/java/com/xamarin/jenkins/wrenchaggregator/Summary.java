package com.xamarin.jenkins.wrenchaggregator;

import hudson.Extension;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterDefinition;
import hudson.model.BooleanParameterValue;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.InvisibleAction;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterDefinition;
import hudson.model.PasswordParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.model.TransientProjectActionFactory;
import hudson.util.RunList;
import hudson.util.Secret;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Summary extends InvisibleAction {

    final private AbstractProject<?, ?> project;

    public Summary(@SuppressWarnings("rawtypes") AbstractProject project) {

        this.project = project;
    }

    public RunList<?> getBuilds() {

        return project.getBuilds();
    }

    @Override
    public String toString() {

        return "Job parameter summary for " + project.toString();
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
