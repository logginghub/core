package com.logginghub.utils.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.junit.Test;

import com.logginghub.utils.data.DataElement;

public class TestDataElement {

    @Test public void testDataElement() throws Exception {

        validateEncoding(10d, DataElement.Type.Double);
        validateEncoding(true, DataElement.Type.Boolean);
        validateEncoding(10f, DataElement.Type.Float);
        validateEncoding(10, DataElement.Type.Int);
        validateEncoding(10L, DataElement.Type.Long);
        validateEncoding(null, DataElement.Type.Null);
        validateEncoding((short)10, DataElement.Type.Short);
        validateEncoding("10", DataElement.Type.String);
        

    }

    private void validateEncoding(Object object, DataElement.Type type) throws Exception {

        DataElement element = new DataElement(object, type);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        element.write(dos);

        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(baos.toByteArray()));
        DataElement read = DataElement.read(dis);

        assertThat(read.type, is(type));
        assertThat(read.object, is(object));

    }

}
