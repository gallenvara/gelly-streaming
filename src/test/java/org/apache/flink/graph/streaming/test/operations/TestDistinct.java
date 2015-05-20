/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.graph.streaming.test.operations;

import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.streaming.EdgeOnlyStream;
import org.apache.flink.graph.streaming.test.GraphStreamTestUtils;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.test.util.MultipleProgramsTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

@RunWith(Parameterized.class)
public class TestDistinct extends MultipleProgramsTestBase {

	public TestDistinct(TestExecutionMode mode) {
		super(mode);
	}

	private String resultPath;
	private String expectedResult;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void before() throws Exception {
		resultPath = tempFolder.newFile().toURI().toString();
	}

	@After
	public void after() throws Exception {
		compareResultsByLinesInMemory(expectedResult, resultPath);
	}

	@Test
	public void testDistinct() throws Exception {
		/*
		 * Test distinct() with the sample graph duplicated
	     */
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		List<Edge<Long, Long>> edges = GraphStreamTestUtils.getLongLongEdges();
		edges.addAll(GraphStreamTestUtils.getLongLongEdges());

		EdgeOnlyStream<Long, Long> graph =
				new EdgeOnlyStream<>(env.fromCollection(edges), env);

		graph.distinct().getEdges()
				.writeAsCsv(resultPath, FileSystem.WriteMode.OVERWRITE);

		env.execute();
		expectedResult = "1,2,12\n" +
				"1,3,13\n" +
				"2,3,23\n" +
				"3,4,34\n" +
				"3,5,35\n" +
				"4,5,45\n" +
				"5,1,51\n";
	}
}
