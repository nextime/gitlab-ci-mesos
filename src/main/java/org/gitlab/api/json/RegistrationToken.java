package org.gitlab.api.json;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 *
 * @author Tomas Barton
 */
public class RegistrationToken extends GenericJson {

    @Key("id")
    public int id;

    @Key("token")
    public String token;
}
