/*
 * Copyright 2019 Averbis GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.averbis.tutorials;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.XmlCasDeserializer;
import org.apache.uima.util.XmlCasSerializer;
import org.xml.sax.SAXException;

public class UIMARestClientAnnotator extends JCasAnnotator_ImplBase {

	public static final String PARAM_URL = "url";
	@ConfigurationParameter(
			name = PARAM_URL,
			mandatory = true,
			defaultValue = "http://localhost:5000/rest/textanalysis/analyseText/",
			description = "The remote annotator endpoint URL")
	private String url;
	
	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {
		
		try {
			OutputStream outputStream = new ByteArrayOutputStream();
			XmlCasSerializer.serialize(jcas.getCas(), outputStream);
			String xmi = outputStream.toString();
		
			Client client = ClientBuilder.newClient();
			Response response = client.target(this.url).request(MediaType.APPLICATION_XML).post(Entity.entity(xmi, MediaType.APPLICATION_XML));
			
			xmi = response.readEntity(String.class);
			InputStream inputStream = IOUtils.toInputStream(xmi);
			XmlCasDeserializer.deserialize(inputStream, jcas.getCas());
			
		} catch (SAXException | IOException e) {
			
			throw new AnalysisEngineProcessException(e.getCause());
		}
	}

}
