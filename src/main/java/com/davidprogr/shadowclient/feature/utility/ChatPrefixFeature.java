package com.davidprogr.shadowclient.feature.utility;

import com.davidprogr.shadowclient.feature.Feature;

/**
 * Adds a custom prefix to outgoing chat messages. Handled in ChatScreenMixin.
 */
public class ChatPrefixFeature extends Feature {

    private String prefix = "[Shadow] ";

    public ChatPrefixFeature() {
        super("ChatPrefix","Adds a prefix to chat messages", Category.UTILITY);
    }

    public String getPrefix() { return prefix; }
    public void   setPrefix(String p) { this.prefix = p; }
}
