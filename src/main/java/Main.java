import org.apache.pdfbox.exceptions.COSVisitorException;

import java.io.IOException;

/**
 * Created by StancuFlorin on 6/25/2015.
 */

public class Main {

    public static void main(String[] args) throws IOException, COSVisitorException {

        TextReplace textReplace = new TextReplace("file.pdf", "Download from Wow! eBook <www.wowebook.com>");
        textReplace.execute();

    }

}
