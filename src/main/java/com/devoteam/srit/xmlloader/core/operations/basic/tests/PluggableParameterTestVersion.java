/*
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *
 * This file is part of Multi-Protocol Test Suite (MTS).
 *
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.devoteam.srit.xmlloader.core.operations.basic.tests;

import com.devoteam.srit.xmlloader.core.Parameter;
import com.devoteam.srit.xmlloader.core.Runner;
import com.devoteam.srit.xmlloader.core.exception.AssertException;
import com.devoteam.srit.xmlloader.core.exception.ParameterException;
import com.devoteam.srit.xmlloader.core.operations.basic.operators.AbstractPluggableParameterOperator;
import com.devoteam.srit.xmlloader.core.pluggable.PluggableName;

import java.util.Map;

/**
 * @author EmanuelMoreira
 */
public class PluggableParameterTestVersion extends AbstractPluggableParameterTest {
    final private String NAME_V_EQUALS = "version.equals";
    final private String NAME_V_COMPARE = "version.compare";

    public PluggableParameterTestVersion() {
        this.addPluggableName(new PluggableName(NAME_V_EQUALS));
        this.addPluggableName(new PluggableName(NAME_V_COMPARE));
    }

    @Override
    public void test(Runner runner, Map<String, Parameter> operands, String name, String parameter) throws Exception {
        try {
            AbstractPluggableParameterOperator.normalizeParameters(operands);
        } catch (ParameterException e) {
            throw new AssertException(e.getMessage(), e);
        }

        Parameter param = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "parameter");
        Parameter testValue = AbstractPluggableParameterOperator.assertAndGetParameter(operands, "value");

        int length = param.length();
        if (0 == length) throw new AssertException("A test between empty parameters is a KO");
        for (int i = 0; i < length; i++) {
            if (name.equalsIgnoreCase(NAME_V_EQUALS)) {
                String paramString = param.get(i).toString();
                String[] currentVersions = paramString.split("\\.");
                currentVersions = cleanVersion(currentVersions);

                if (paramString.equals("") || paramString.matches("\\$\\{.*\\}") || paramString.matches("\\[.*\\]")) {
                    throw new AssertException("Error in version test " + name + "\n" + param + "\n" + param.get(i) + " is invalid\n");
                }

                String testValueString = testValue.get(i).toString();
                String[] lowerVersions = testValueString.split("\\.");
                lowerVersions = cleanVersion(lowerVersions);

                String lowerVersion = "";
                for (int j = 0; j < lowerVersions.length; j++) {
                    lowerVersion += lowerVersions[j] + ".";
                }
                lowerVersion = lowerVersion.substring(0, lowerVersion.length() - 1);
                String currentVersion = "";
                for (int j = 0; j < currentVersions.length; j++) {
                    currentVersion += currentVersions[j] + ".";
                }
                currentVersion = currentVersion.substring(0, currentVersion.length() - 1);
                if (!lowerVersion.equals(currentVersion)) {
                    throw new AssertException("Error in version test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is not equal to \n" + testValue.get(i));
                }
            } else if (name.equalsIgnoreCase(NAME_V_COMPARE)) {
                String paramString = param.get(i).toString();
                String[] currentVersions = paramString.split("\\.");
                currentVersions = cleanVersion(currentVersions);

                if (paramString.equals("") || paramString.matches("\\$\\{.*\\}") || paramString.matches("\\[.*\\]")) {
                    throw new AssertException("Error in version test " + name + "\n" + param + "\n" + param.get(i) + " is invalid\n");
                }

                String testValueString = testValue.get(i).toString();
                String[] versions = testValueString.split("\\;");
                if (versions.length != 1 && versions.length != 2) {
                    throw new AssertException("Error in version test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + testValue.get(i) + "\n" + " Bad Format in Value, should be \"value\"=\"minor;major\" or just  \"value\"=\"minor\"\n");
                }
                String[] lowerVersions = versions[0].split("\\.");
                lowerVersions = cleanVersion(lowerVersions);

                int lowerLen = Math.min(lowerVersions.length, currentVersions.length);
                for (int j = 0; j < lowerLen; j++) {
                    Integer lower = Integer.parseInt(lowerVersions[j]);
                    Integer current = Integer.parseInt(currentVersions[j]);
                    if (current.compareTo(lower) > 0) {
                        break;
                    }
                    if (current.compareTo(lower) < 0) {
                        throw new AssertException("Error in version test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is lower then \n" + testValue.get(i));
                    }
                    if (current.compareTo(lower) == 0) {
                        if ((j == (lowerLen - 1)) && (lowerVersions.length > currentVersions.length)) {
                            throw new AssertException("Error in version test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " is lower then \n" + testValue.get(i));
                        }
                    }
                }

                // If higherVersion exists
                if (versions.length == 2) {
                    if (!versions[1].equals("")) {
                        String[] higherVersions = versions[1].split("\\.");
                        higherVersions = cleanVersion(higherVersions);
                        int higherLen = Math.min(higherVersions.length, currentVersions.length);
                        for (int j = 0; j < higherLen; j++) {
                            Integer current = Integer.parseInt(currentVersions[j]);
                            Integer higher = Integer.parseInt(higherVersions[j]);
                            if (current.compareTo(higher) < 0) {
                                break;
                            }
                            if (current.compareTo(higher) > 0) {
                                throw new AssertException("Error in version test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " isn't in the interval [ " + testValue.get(i) + " [\n");
                            }
                            if (current.compareTo(higher) == 0) {
                                if ((j == (higherLen - 1)) && (higherVersions.length <= currentVersions.length)) {
                                    throw new AssertException("Error in version test " + name + " between \n" + param + "and\n" + testValue + "\n" + param.get(i) + " isn't in the interval [ " + testValue.get(i) + " [\n");
                                }
                            }
                        }
                    }
                }
            } else {
                throw new RuntimeException("Unsupported <test> operation for condition = " + name);
            }

        }
    }

    // Function to perform removal of 0s
    private String[] cleanVersion(String[] version) {
        while (version[version.length - 1].equals("0")) {
            String[] aux = new String[version.length - 1];
            for (int i = 0; i < aux.length; i++) {
                aux[i] = version[i];
            }
            version = aux;
        }
        return version;
    }

}
