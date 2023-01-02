// Copyright © 2012-2023 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.xoom.common.config;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EnvVarPropertiesTest {
    @Rule
    public final EnvironmentVariables envVars = new EnvironmentVariables();

    @Test
    public void envVarIsInterpolated() {
        envVars.set("FOO", "bar");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${FOO}");

        assertEquals("bar", props.getProperty("p"));
    }

    @Test
    public void multipleEnvVarsAreInterpolated() {
        envVars.set("FOO", "baz");
        envVars.set("BAR", "qux");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${FOO}${BAR}");

        assertEquals("bazqux", props.getProperty("p"));
    }

    @Test
    public void envVarsAsSubstringAreInterpolated() {
        envVars.set("NOUN", "base");
        envVars.set("VERB", "belong");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "All your ${NOUN} are ${VERB} to us");

        assertEquals("All your base are belong to us", props.getProperty("p"));
    }

    @Test
    public void lowerCaseEnvVarsAreSupported() {
        envVars.set("foo", "bar");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${foo}");

        assertEquals("bar", props.getProperty("p"));
    }

    @Test
    public void envVarsContainingDigitsAreSupported() {
        envVars.set("F00", "baz");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${F00}");

        assertEquals("baz", props.getProperty("p"));
    }

    @Test
    public void envVarsContainingUnderscoresAreSupported() {
        envVars.set("FOO_BAR", "baz");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${FOO_BAR}");

        assertEquals("baz", props.getProperty("p"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void inexistentEnvVarsThrow() {
        envVars.set("FOO", null);

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${FOO}");

        fail("Interpolating an inexistent env var should have thrown an exception");
    }

    @Test
    public void defaultsAreNotAppliedIfEnvIsSet() {
        envVars.set("FOO", "bar");

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${FOO:unused default}");
        assertEquals("bar", props.getProperty("p"));
    }

    @Test
    public void defaultsAreAppliedIfEnvIsNotSet() {
        envVars.set("FOO", null);

        Properties props = new EnvVarProperties();
        props.setProperty("p", "${FOO:I am the default}");
        assertEquals("I am the default", props.getProperty("p"));
    }

}
