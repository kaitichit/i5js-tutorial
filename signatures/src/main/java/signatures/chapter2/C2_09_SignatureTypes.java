/*
 * This class is part of the white paper entitled
 * "Digital Signatures for PDF documents"
 * written by Bruno Lowagie
 * 
 * For more info, go to: http://itextpdf.com/sales
 */
package signatures.chapter2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

public class C2_09_SignatureTypes {

	public static final String KEYSTORE = "src/main/resources/ks";
	public static final char[] PASSWORD = "password".toCharArray();
	public static final String SRC = "src/main/resources/hello.pdf";
	public static final String DEST = "results/chapter2/hello_level_%s.pdf";
	
	public void sign(PrivateKey pk, Certificate[] chain,
			String src, String dest, String provider,
			String reason, String location, int certificationLevel,
			String digestAlgorithm, CryptoStandard subfilter)
					throws GeneralSecurityException, IOException, DocumentException {
        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 748, 144, 780), 1, "sig");
        appearance.setCertificationLevel(certificationLevel);
        // Creating the signature
        PrivateKeySignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
        MakeSignature.signDetached(appearance, pks, chain, null, null, null, 0, subfilter);
	}
	
	public void addText(String src, String dest) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest), '\0', true);
		ColumnText.showTextAligned(stamper.getOverContent(1), Element.ALIGN_LEFT, new Phrase("TOP SECRET"), 36, 820, 0);
		stamper.close();
	}
	
	public void addAnnotation(String src, String dest) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest), '\0', true);
		PdfAnnotation comment = PdfAnnotation.createText(stamper.getWriter(),
				new Rectangle(200, 800, 250, 820), "Finally Signed!",
				"Bruno Specimen has finally signed the document", true, "Comment");
		stamper.addAnnotation(comment, 1);
		stamper.close();
	}
	
	public void addWrongAnnotation(String src, String dest) throws IOException, DocumentException {
		PdfReader reader = new PdfReader(src);
		PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
		PdfAnnotation comment = PdfAnnotation.createText(stamper.getWriter(),
				new Rectangle(200, 800, 250, 820), "Finally Signed!",
				"Bruno Specimen has finally signed the document", true, "Comment");
		stamper.addAnnotation(comment, 1);
		stamper.close();
	}
	
	public void signAgain(PrivateKey pk, Certificate[] chain,
			String src, String dest, String provider,
			String reason, String location,
			String digestAlgorithm, CryptoStandard subfilter)
					throws GeneralSecurityException, IOException, DocumentException {
        // Creating the reader and the stamper
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);
        // Creating the appearance
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setReason(reason);
        appearance.setLocation(location);
        appearance.setVisibleSignature(new Rectangle(36, 700, 144, 732), 1, "Signature2");
        // Creating the signature
        PrivateKeySignature pks = new PrivateKeySignature(pk, digestAlgorithm, provider);
        MakeSignature.signDetached(appearance, pks, chain, null, null, null, 0, subfilter);
	}
	
	public static void main(String[] args) throws GeneralSecurityException, IOException, DocumentException {
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String)ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        Certificate[] chain = ks.getCertificateChain(alias);
		C2_09_SignatureTypes app = new C2_09_SignatureTypes();
		app.sign(pk, chain, SRC, String.format(DEST, 1), provider.getName(), "Test 1", "Ghent", PdfSignatureAppearance.NOT_CERTIFIED, DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.sign(pk, chain, SRC, String.format(DEST, 2), provider.getName(), "Test 2", "Ghent", PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS, DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.sign(pk, chain, SRC, String.format(DEST, 3), provider.getName(), "Test 3", "Ghent", PdfSignatureAppearance.CERTIFIED_FORM_FILLING, DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.sign(pk, chain, SRC, String.format(DEST, 4), provider.getName(), "Test 4", "Ghent", PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED, DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.addWrongAnnotation(String.format(DEST, 1), String.format(DEST, "1_annotated_wrong"));
		app.addAnnotation(String.format(DEST, 1), String.format(DEST, "1_annotated"));
		app.addAnnotation(String.format(DEST, 2), String.format(DEST, "2_annotated"));
		app.addAnnotation(String.format(DEST, 3), String.format(DEST, "3_annotated"));
		app.addAnnotation(String.format(DEST, 4), String.format(DEST, "4_annotated"));
		app.addText(String.format(DEST, 1), String.format(DEST, "1_text"));
		app.signAgain(pk, chain, String.format(DEST, 1), String.format(DEST, "1_double"), provider.getName(), "Second signature test", "Gent", DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.signAgain(pk, chain, String.format(DEST, 2), String.format(DEST, "2_double"), provider.getName(), "Second signature test", "Gent", DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.signAgain(pk, chain, String.format(DEST, 3), String.format(DEST, "3_double"), provider.getName(), "Second signature test", "Gent", DigestAlgorithms.SHA256, CryptoStandard.CMS);
		app.signAgain(pk, chain, String.format(DEST, 4), String.format(DEST, "4_double"), provider.getName(), "Second signature test", "Gent", DigestAlgorithms.SHA256, CryptoStandard.CMS);
	}
}
