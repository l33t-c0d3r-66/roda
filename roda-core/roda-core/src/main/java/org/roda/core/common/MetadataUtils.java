/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.core.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;

import org.roda.core.data.v2.validation.ValidationException;
import org.roda.core.data.v2.validation.ValidationIssue;
import org.roda.core.data.v2.validation.ValidationReport;
import org.roda.core.storage.ContentPayload;
import org.roda.core.storage.InputStreamContentPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an utility class for metadata helpers.
 *
 * @author Luis Faria <lfaria@keep.pt>
 */
public final class MetadataUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataUtils.class);

  /** Private empty constructor */
  private MetadataUtils() {

  }

  public static ContentPayload saveToContentPayload(final JAXBElement<?> object, final Class<?> tClass) {
    return new InputStreamContentPayload(() -> {
      try {
        return createInputStream(object, tClass);
      } catch (ValidationException e) {
        throw new IOException(e);
      }
    });
  }

  public static InputStream createInputStream(final JAXBElement<?> object, final Class<?> tClass)
    throws ValidationException {
    try {
      StringWriter writer = new StringWriter();
      JAXBContext jaxbContext = JAXBContext.newInstance(tClass);
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.marshal(object, writer);
      return new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));

    } catch (JAXBException e) {
      throw new ValidationException(e.getMessage());
    }
  }

  public static ValidationReport xmlValidationErrorsToValidationReport(ValidationEventCollector validationCollector) {
    ValidationReport report = new ValidationReport();
    report.setValid(false);
    List<ValidationIssue> issues = new ArrayList<>();
    for (ValidationEvent event : validationCollector.getEvents()) {
      ValidationIssue issue = new ValidationIssue();
      issue.setMessage(event.getMessage());
      issue.setColumnNumber(event.getLocator().getColumnNumber());
      issue.setLineNumber(event.getLocator().getLineNumber());
      issues.add(issue);
    }

    report.setIssues(issues);
    return report;
  }
}
