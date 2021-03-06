/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package storm.hdfs.spout;

import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.List;

interface FileReader {
  Path getFilePath();

  /**
   * A simple numeric value may not be sufficient for certain formats consequently
   * this is a String.
   */
  FileOffset getFileOffset();

  /**
   * Get the next tuple from the file
   *
   * @return null if no more data
   * @throws IOException
   */
  List<Object> next() throws IOException, ParseException;

  /**
   * Get the next group by a given fields from the file.
   * Specific in the {@link util.Configuration}.
   * You can change this value by
   *<pre>
   *     <code>
   *         Configuration.DATA_TIMEFIELDS_INDEX = ...;
   *     </code>
   *</pre>
   * or configure in <code>configuration.xml</code>
   * @return
   */
  List<Object> nextByDefaultFields() throws IOException, ParseException;
  Object getKey();
  void close();
}
