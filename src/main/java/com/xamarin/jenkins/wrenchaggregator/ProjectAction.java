/*
The MIT License

Copyright (c) 2013, Red Hat, Inc.
Copyright (c) 2016, Xamarin, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.xamarin.jenkins.wrenchaggregator;

import com.xamarin.jenkins.wrenchaggregator.Summary.SummaryFactory;
import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.Combination;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildWrapper;
import java.io.IOException;
import org.kohsuke.stapler.DataBoundConstructor;

public class ProjectAction extends BuildWrapper {

    @DataBoundConstructor
    public ProjectAction() {
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    SummaryFactory sf = new SummaryFactory();
    Combination Axis = sf.getAxis();

    @Override
    public Action getProjectAction(AbstractProject project) {
        return new Summary(project, Axis);
    }

    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        return new NoopEnv();
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<BuildWrapper> {

        public String getDisplayName() {
            // text displayed next to checkbox
            return "Build step display aggregator";
        }
    }

    class NoopEnv extends Environment {
    }

}
