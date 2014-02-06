package org.gitlab.api.json;

import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;

/**
 *
 * @author Tomas Barton
 */
public class Registration extends GenericJson {

    @Key("token")
    public String token;

    @Key("public_key")
    public String publicKey;
}
