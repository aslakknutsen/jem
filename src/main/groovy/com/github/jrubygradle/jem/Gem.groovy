package com.github.jrubygradle.jem

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import groovy.json.JsonOutput
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Plain Old Groovy Object for an enumeration of metadata provided by a gem
 */
@TypeChecked
@CompileStatic
class Gem {
    @JsonProperty
    String name

    @JsonProperty
    Version version

    @JsonProperty
    String description

    @JsonProperty
    String platform

    @JsonProperty
    Object email

    @JsonProperty
    String homepage

    @JsonProperty
    List<String> authors = []

    @JsonProperty
    List<String> files

    @JsonProperty(value='test_files')
    List<String> testFiles

    @JsonProperty
    List<String> executables

    @JsonProperty
    String bindir

    @JsonProperty(value='require_paths')
    List<String> requirePaths

    @JsonProperty
    List<String> licenses

    @JsonProperty(value='specification_version')
    Integer specificationVersion

    @JsonProperty(value='rubygems_version')
    String rubygemsVersion

    /**
     * Take the given argument and produce a {@code Gem} instance
     *
     * @param metadata a {@code java.lang.String}, a {@code java.io.File} or a {@code java.util.zip.GZIPInputStream}
     * @return
     */
    static Gem fromFile(Object metadata) {
        if (metadata instanceof String) {
            return createGemFromFile(new File(metadata))
        }
        if (metadata instanceof File) {
            return createGemFromFile(metadata as File)
        }
        if (metadata instanceof InputStream) {
            return createGemFromInputStream(metadata as InputStream)
        }

        return null
    }

    /**
     * Output the gemspec stub for this file
     *
     * See <https://github.com/rubygems/rubygems/blob/165030689defe16680b7f336232db62024f49de4/lib/rubygems/specification.rb#L2422-L2512>
     *
     * @return
     */
    String toRuby() {
        return """\
# -*- encoding: utf-8 -*-
# stub: ${name} ${version.version} ${platform} ${requirePaths.join("\0")}
#
# NOTE: This specification was generated by groovy-gem
#           <https://github.com/jruby-gradle/groovy-gem>

Gem::Specification.new do |s|
  s.name = ${sanitize(name)}
  s.version = ${sanitize(version.version)}
  s.description = ${sanitize(description)}
  s.homepage = ${sanitize(homepage)}
  s.authors = ${sanitize(authors)}
  s.email = ${sanitize(email)}
  s.licenses = ${sanitize(licenses)}

  s.platform = ${sanitize(platform)}
  s.require_paths = ${sanitize(requirePaths)}
  s.executables = ${sanitize(executables)}
  s.rubygems_version = ${sanitize(rubygemsVersion)}
end
"""
    }

    /** Convert whatever object we're given into a safe (see: JSON) reprepsentation */
    protected String sanitize(Object value) {
        return JsonOutput.toJson(value)
    }

    private static Gem createGemFromFile(File gemMetadataFile) {
        if (!gemMetadataFile.exists()) {
            return null
        }
        return getYamlMapper().readValue(gemMetadataFile, Gem)
    }

    private static Gem createGemFromInputStream(InputStream gemMetadataStream) {
        return getYamlMapper().readValue(gemMetadataStream, Gem)
    }

    private static ObjectMapper getYamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}
