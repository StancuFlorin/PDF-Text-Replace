import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.util.PDFOperator;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by StancuFlorin on 6/27/2015.
 */

public class TextReplace {

    private static final String FILE_OUT = "out.pdf";

    private String fileIn;
    private String regex;
    private String replacement;

    public TextReplace(String fileIn, String regex, String replacement) {
        this.fileIn = fileIn;
        this.regex = regex;
        this.replacement = replacement;
    }

    public TextReplace(String fileIn, String regex) {
        this(fileIn, regex, "");
    }

    private PDDocument read() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        return PDDocument.load(classLoader.getResource(fileIn));
    }

    private String regex(String string) {
        return string.replaceFirst(regex, replacement);
    }

    public void execute() throws IOException, COSVisitorException {

        PDDocument document = read();
        List pages = document.getDocumentCatalog().getAllPages();
        for (int i = 0; i < pages.size(); i++) {

            PDPage page = (PDPage) pages.get(i);
            PDStream contents = page.getContents();
            PDFStreamParser parser = new PDFStreamParser(contents.getStream());
            parser.parse();

            List tokens = parser.getTokens();
            for (int j = 0; j < tokens.size(); j++) {

                Object next = tokens.get(j);
                if (next instanceof PDFOperator) {

                    // Tj and TJ are the two operators that display strings in a PDF
                    PDFOperator operator = (PDFOperator) next;
                    if (operator.getOperation().equals("Tj")) {

                        COSString previous = (COSString) tokens.get(j - 1);
                        String string = previous.getString();
                        string = regex(string);
                        previous.reset();
                        previous.append(string.getBytes("ISO-8859-1"));

                    } else if (operator.getOperation().equals("TJ")) {

                        COSArray previous = (COSArray) tokens.get(j - 1);
                        for (int k = 0; k < previous.size(); k++) {

                            Object arrElement = previous.getObject(k);
                            if (arrElement instanceof COSString) {
                                COSString cosString = (COSString) arrElement;
                                String string = cosString.getString();
                                string = regex(string);
                                cosString.reset();
                                cosString.append(string.getBytes("ISO-8859-1"));
                            }

                        }

                    }

                }

            }

            // now that the tokens are updated we will replace the page content stream.
            PDStream updatedStream = new PDStream(document);
            OutputStream outputStream = updatedStream.createOutputStream();
            ContentStreamWriter tokenWriter = new ContentStreamWriter(outputStream);
            tokenWriter.writeTokens(tokens);
            page.setContents(updatedStream);

        }

        document.save(FILE_OUT);
        document.close();

    }

}
