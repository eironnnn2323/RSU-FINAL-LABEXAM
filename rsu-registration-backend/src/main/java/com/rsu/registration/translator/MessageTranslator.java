package com.rsu.registration.translator;

/**
 * Message Translator Pattern - Interface
 * Converts messages between different formats
 */
public interface MessageTranslator<S, T> {
    
    /**
     * Translate from source format to target format
     * @param source Source message
     * @return Translated message in target format
     */
    T translate(S source);
    
    /**
     * Get the name of this translator
     * @return Translator name
     */
    String getTranslatorName();
    
    /**
     * Get source format name
     * @return Source format
     */
    String getSourceFormat();
    
    /**
     * Get target format name
     * @return Target format
     */
    String getTargetFormat();
}
