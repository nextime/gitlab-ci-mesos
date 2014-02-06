package org.gitlab.api.json;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 *
 * @author Tomas Barton
 */
public class PushBuild extends GenericJson {

    @Key("token")
    public String token;

    @Key("state")
    public String state;

    @Key("trace")
    public String trace;

}
