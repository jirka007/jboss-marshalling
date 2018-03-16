/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.test.marshalling;

import org.jboss.marshalling.Marshaller;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;
import org.jboss.marshalling.Unmarshaller;
import org.jboss.marshalling.cloner.ClonerConfiguration;
import org.jboss.marshalling.cloner.ObjectCloner;
import org.jboss.marshalling.cloner.ObjectClonerFactory;
import org.jboss.marshalling.cloner.ObjectCloners;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * OtherMarshallerTests
 */
public final class OtherMarshallerTests extends TestBase {

    public OtherMarshallerTests(TestMarshallerProvider testMarshallerProvider, TestUnmarshallerProvider testUnmarshallerProvider, MarshallingConfiguration configuration) {
        super(testMarshallerProvider, testUnmarshallerProvider, configuration);
    }

    @Test
    public synchronized void testPriorityBlockingQueue() throws Throwable {
        final ObjectClonerFactory clonerFactory = ObjectCloners.getSerializingObjectClonerFactory();
        final ClonerConfiguration configuration = new ClonerConfiguration();
        final ObjectCloner cloner = clonerFactory.createCloner(configuration);
        PriorityBlockingQueueTestObject testObject = new PriorityBlockingQueueTestObject<Integer>();
        if(testObject != null) {
            testObject.add(new Integer(100));
            cloner.clone(testObject);
        }
    }

    public static class PriorityBlockingQueueTestObject<T> implements Serializable {
        private PriorityBlockingQueue<T> queue = new PriorityBlockingQueue<T>();
        public void add(T item) {
            this.queue.add(item);
        }
    }

    /**
     * Test for JBMAR-218
     *
     * @throws Exception
     */
    @Test
    public void objectReplacementWithNullValue() throws Exception {
        C1 object = new C1();
        write(object, "marshall.ser");
        C1 object2 = (C1) read("marshall.ser");

        // this was cloning without issue, so the bug appears to only be when marshalling above
        C1 objectClone = (C1) clone(object);
    }

    public void write(Object object, String file) throws Exception {

        // Get the factory for the "river" marshalling protocol
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("river");

        // Create a configuration
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        // Use version 3
        //configuration.setVersion(3);
        final Marshaller marshaller = marshallerFactory.createMarshaller(configuration);
        final FileOutputStream os = new FileOutputStream(file);
        try {
            marshaller.start(Marshalling.createByteOutput(os));
            marshaller.writeObject(object);
            marshaller.finish();
            os.close();
        } finally {
            // clean up stream resource
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Object read(String file) throws Exception {
        // Get the factory for the "river" marshalling protocol
        final MarshallerFactory marshallerFactory = Marshalling.getProvidedMarshallerFactory("river");

        // Create a configuration
        final MarshallingConfiguration configuration = new MarshallingConfiguration();
        Object object = null;
        final Unmarshaller unmarshaller = marshallerFactory.createUnmarshaller(configuration);
        final FileInputStream is = new FileInputStream(file);
        try {
            unmarshaller.start(Marshalling.createByteInput(is));
            object =  unmarshaller.readObject();
            unmarshaller.finish();
            is.close();
        } finally {
            // clean up stream resource
            try {
                is.close();
            } catch (IOException e) {
                System.err.print("Stream close failed: ");
                e.printStackTrace();
            }
        }
        return object;
    }

    public Object clone(Object object) throws Exception {
        final ObjectClonerFactory clonerFactory = ObjectCloners.getSerializingObjectClonerFactory();
        final ClonerConfiguration configuration = new ClonerConfiguration();
        final ObjectCloner cloner = clonerFactory.createCloner(configuration);
        return cloner.clone(object);
    }

    public class C1 implements Serializable {
        C2 x, y;
        public C1() {
            x = new C2();
            y = x;
        }
    }

    public class C2 implements Serializable {
        Object readResolve() {
            return null;
        }
    }

}
