package net.hedelius.itello;

import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.mockito.InOrder;
import se.itello.example.payments.PaymentReceiver;

import javax.xml.bind.DatatypeConverter;

public class ItelloTest {

    private IPaymentFileHandler sut;
    private PaymentReceiver paymentReceiver;
    private FileReader fileReader;

    @Before
    public void setup() {

        fileReader = mock(FileReader.class);
        paymentReceiver = mock(PaymentReceiver.class);
        sut = new WholeFileHandler(fileReader, paymentReceiver);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownFileType() {

        // arrange
        String unknownFile = "Some_random_file_unsupported.txt";

        // act
        sut.handleFile(unknownFile);

        // assert - should not get here
        fail();
    }

    @Test
    public void testBetalningTestData()
    {
        // arrange
        String fileName = "foobar_betalningsservice.txt";
        String testDataHex =
                "4f35353535203535353535353535353520202020202020343731312c3137" +
                "20202020202020202034323031313033313553454b0d0a42202020202020" +
                "202020203330303031323334353637383930202020202020202020202020" +
                "202020202020202020202020200d0a422020202020202020202031303030" +
                "323334353637383930312020202020202020202020202020202020202020" +
                "20202020200d0a4220202020202020203330302c31303334353637383930" +
                "3132202020202020202020202020202020202020202020202020200d0a42" +
                "20202020202020203430302c303734353637383930313233202020202020" +
                "202020202020202020202020202020202020200d0a";
        byte[] testDataBytes = DatatypeConverter.parseHexBinary(testDataHex);
        when(fileReader.read(fileName)).thenReturn(testDataBytes);

        // act
        sut.handleFile(fileName);

        // assert
        String expectedAccount = "5555 5555555555";
        Date expectedDate = new GregorianCalendar(2011, 2, 15).getTime();
        String expectedCurrency = "SEK";
        InOrder inOrder = inOrder(paymentReceiver);
        inOrder.verify(paymentReceiver).startPaymentBundle(expectedAccount, expectedDate, expectedCurrency);
        inOrder.verify(paymentReceiver).payment(new BigDecimal(3000), "1234567890");
        inOrder.verify(paymentReceiver).payment(new BigDecimal(1000), "2345678901");
        inOrder.verify(paymentReceiver).payment(new BigDecimal(300.10), "3456789012");
        inOrder.verify(paymentReceiver).payment(new BigDecimal(400.07), "4567890123");
    }
}
