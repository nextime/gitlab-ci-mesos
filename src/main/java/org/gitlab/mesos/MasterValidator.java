package org.gitlab.mesos;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
 *
 * @author Tomas Barton
 */
public class MasterValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        System.out.println("master is: " + value);

        // TODO: should be IP or Zookeeper URL
        /*
         throw new ParameterException("Parameter " + name + " should be between 1 and 12");
         */
    }
}
