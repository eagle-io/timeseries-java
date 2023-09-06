/**
 * JSON Time Series (JTS).
 * <p>
 * JSON Time Series (JTS) is a text format which contains time series data represented as JSON.
 * <p>
 * An example JTS document:
 * <pre>
 * {
 * "docType" : "jts",
 * "version": "1.0",
 * "data" :
 * {
 * "0" : { "ts" : "2012-10-25T23:57:29.087-07:00", "f" : { "1" : { "v" : "myValue1", "q" : "myQuality1" }, "2" : { "v" : "myValue2", "q" : "myQuality2" } } },
 * "1" : { "ts" : "2012-10-25T23:57:29.379-07:00", "f" : { "1" : { "v" : "myValue1", "q" : "myQuality1" }, "2" : { "v" : "myValue2", "q" : "myQuality2" } } },
 * "2" : { "ts" : "2012-10-25T23:57:29.379-07:00", "f" : { "1" : { "v" : "myValue1", "q" : "myQuality1" }, "2" : { "v" : "myValue2", "q" : "myQuality2" } } }
 * }
 * }
 * </pre>
 * <p>
 * In the above document:
 * <ul>
 * <li><b>{@code doctype}</b> is the format and should always be <b>{@code jts}</b>
 * <li><b>{@code version}</b> is the version of JTS
 * <li><b>{@code data}</b> is a JSON document containing a list of timestamped records
 * <li>for each record in the list, the record key is a 0-based index representing the record number, and the record value is a JSON document containing:
 * <ul>
 * <li><b>{@code ts}</b> is the timestamp as an ISO string
 * <li><b>{@code f}</b> is a JSON document containing a list of fields
 * <li>for each field in the list, the field key is a 1-based index representing the column number, and the field value is a JSON document containing:
 * <ul>
 * <li><b>{@code v}</b> is the actual data value as a string
 * <li><b>{@code q}</b> is optional quality data
 * </ul>
 * </ul>
 * </ul>
 *
 * @author <a href="mailto:jesse@argos.io">Jesse Mitchell</a>
 */
package io.eagle.util.jts;
