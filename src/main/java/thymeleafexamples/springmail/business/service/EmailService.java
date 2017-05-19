/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package thymeleafexamples.springmail.business.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import fr.afcepf.model.object.Article;
import fr.afcepf.model.object.Order;
import fr.afcepf.model.object.User;
import fr.afcepf.pdf.generation.PdfGeneration;
import thymeleafexamples.springmail.business.SpringMailConfig;

@Service
public class EmailService {
	/**
	 * Logger pour tracker le send mail with Pdf.
	 */
	private static final Logger log = Logger.getLogger(EmailService.class);
	private static final String EMAIL_TEXT_TEMPLATE_NAME = "text/email-text";
	private static final String EMAIL_SIMPLE_TEMPLATE_NAME = "html/email-simple";
	private static final String EMAIL_WITHATTACHMENT_TEMPLATE_NAME = "html/email-withattachment";
	private static final String EMAIL_WITHPDF_TEMPLATE_NAME = "html/email-withpdf";
	private static final String EMAIL_INLINEIMAGE_TEMPLATE_NAME = "html/email-inlineimage";
	private static final String EMAIL_EDITABLE_TEMPLATE_CLASSPATH_RES = "classpath:mail/editablehtml/email-editable.html";


	//TODO: Modify absolute file_location to where pdf-template is generating the pdf.
	private static final String FILE_LOCATION = "C:/Users/Metabeen/git/Projet_AL30_Projet5/pdf-template/tmp/Facture.pdf";

	private static final String BACKGROUND_IMAGE = "mail/editablehtml/images/background.png";
	private static final String LOGO_BACKGROUND_IMAGE = "mail/editablehtml/images/logo-background.png";
	private static final String THYMELEAF_BANNER_IMAGE = "mail/editablehtml/images/thymeleaf-banner.png";
	private static final String THYMELEAF_LOGO_IMAGE = "mail/editablehtml/images/thymeleaf-logo.png";

	private static final String PNG_MIME = "image/png";

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private TemplateEngine htmlTemplateEngine;

	@Autowired
	private TemplateEngine textTemplateEngine;

	@Autowired
	private TemplateEngine stringTemplateEngine;

	/* 
	 * Send plain TEXT mail 
	 */
	public void sendTextMail(
			final String recipientName, final String recipientEmail, final Locale locale)
					throws MessagingException {

		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", recipientName);
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
		message.setSubject("Example plain TEXT email");
		message.setFrom("thymeleaf@example.com");
		message.setTo(recipientEmail);

		// Create the plain TEXT body using Thymeleaf
		final String textContent = this.textTemplateEngine.process(EMAIL_TEXT_TEMPLATE_NAME, ctx);
		message.setText(textContent);

		// Send email
		this.mailSender.send(mimeMessage);
	}


	/* 
	 * Send HTML mail (simple) 
	 */
	public void sendSimpleMail(
			final String recipientName, final String recipientEmail, final Locale locale)
					throws MessagingException {

		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", recipientName);
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "UTF-8");
		message.setSubject("Example HTML email (simple)");
		message.setFrom("thymeleaf@example.com");
		message.setTo(recipientEmail);

		// Create the HTML body using Thymeleaf
		final String htmlContent = this.htmlTemplateEngine.process(EMAIL_SIMPLE_TEMPLATE_NAME, ctx);
		message.setText(htmlContent, true /* isHtml */);

		// Send email
		this.mailSender.send(mimeMessage);
	}


	/* 
	 * Send HTML mail with attachment. 
	 */
	public void sendMailWithAttachment(
			final String recipientName, final String recipientEmail, final String attachmentFileName,
			final byte[] attachmentBytes, final String attachmentContentType, final Locale locale)
					throws MessagingException {

		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", recipientName);
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message
		= new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
		message.setSubject("Example HTML email with attachment");
		message.setFrom("thymeleaf@example.com");
		message.setTo(recipientEmail);

		// Create the HTML body using Thymeleaf
		final String htmlContent = this.htmlTemplateEngine.process(EMAIL_WITHATTACHMENT_TEMPLATE_NAME, ctx);
		message.setText(htmlContent, true /* isHtml */);

		// Add the attachment
		final InputStreamSource attachmentSource = new ByteArrayResource(attachmentBytes);
		message.addAttachment(
				attachmentFileName, attachmentSource, attachmentContentType);

		// Send mail
		this.mailSender.send(mimeMessage);
	}
	/* 
	 * Send HTML mail with PDF attachment. 
	 */
	public void sendMailWithPdf(String user, String articles, String order, final Locale locale) throws MessagingException {
		log.debug("-------------------Called Method sendMailWithPdf-------------------");
		log.debug("-------------------Set de la variable locale to : " + locale.toString() + "-------------------");
		//Récupération du String JSon et transformation en objet user.
		log.debug("-------------------Début parsing Json-------------------");
		Gson gsonUser = new Gson();
		User userFromJson = gsonUser.fromJson(user,User.class);
		log.debug(userFromJson.toString());
		//Récupération du String JSon sous forme d'array et transformation en list d'articles.
		List<Article> articlesFromJson = new ArrayList<>();
		articlesFromJson.add(new Article("articles1", "prixUnitaire1", "quantiteArticle1", "montantArticle1"));
		articlesFromJson.add(new Article("articles2", "prixUnitaire2", "quantiteArticle2", "montantArticle2"));
		
		//Test de creation d'un JSON array a partir d'un string.
//		JsonParser JP = new JsonParser();
//		JsonElement JE =  JP.parse(articles);
//		JsonArray JA = JE.getAsJsonArray();
		
		
		
//		log.debug(JA);
		//		for (Article article : articlesFromJson) {
		//			log.debug(article.toString());
		//		}
		//Récupération du String Json et transformation en object order.
		Gson gsonOrder = new Gson();
		Order orderFromJson = gsonOrder.fromJson(order, Order.class);
		log.debug(orderFromJson.toString());
		log.debug("-------------------Fin parsing Json-------------------");
		//Creation de la date de la commande
		Date dateCommande = new Date();
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String dateCommandeString = df.format(dateCommande);
		log.debug("Date au format string : " + dateCommandeString);
		//Appel de la méthode pour la génération du PDF
		log.debug("-------------------Début de la génération du pdf-------------------");
		PdfGeneration genPdf = new PdfGeneration();
		genPdf.generatePdf(userFromJson, articlesFromJson, orderFromJson,dateCommandeString);
		log.debug("-------------------Génération du pdf terminée-------------------");
		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", userFromJson.getName() );
		ctx.setVariable("dateCommande", new Date());
		ctx.setVariable("idCommande", orderFromJson.getIdCommande());
		ctx.setVariable("montantCommande", orderFromJson.getMontantCommande());
		
		List<String> articlesForMail = new ArrayList<>();
		articlesForMail.add("article1");
		articlesForMail.add("article2");
		ctx.setVariable("articles", articlesForMail);

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message
		= new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
		message.setSubject("Votre commande n°"+ orderFromJson.getIdCommande() +" sur WineMania a bien été prise en compte!");
		message.setFrom("WineMania");
		message.setTo(userFromJson.getMail());

		// Create the HTML body using Thymeleaf
		final String htmlContent = this.htmlTemplateEngine.process(EMAIL_WITHPDF_TEMPLATE_NAME, ctx);
		message.setText(htmlContent, true /* isHtml */);

		// Add the attachment
		FileInputStream pdf = null;
		byte[] 	barPdf = null;
		try {
			pdf = new FileInputStream(new File(FILE_LOCATION));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			barPdf = IOUtils.toByteArray(pdf);
		} catch (IOException e) {
			e.printStackTrace();

		}
		final InputStreamSource attachmentSource = new ByteArrayResource(barPdf);
		message.addAttachment(
				"Facture.pdf", attachmentSource, "application/pdf");

		// Send mail
		this.mailSender.send(mimeMessage);
	}


	/* 
	 * Send HTML mail with inline image
	 */
	public void sendMailWithInline(
			final String recipientName, final String recipientEmail, final String imageResourceName,
			final byte[] imageBytes, final String imageContentType, final Locale locale)
					throws MessagingException {

		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", recipientName);
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));
		ctx.setVariable("imageResourceName", imageResourceName); // so that we can reference it from HTML

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message
		= new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
		message.setSubject("Example HTML email with inline image");
		message.setFrom("thymeleaf@example.com");
		message.setTo(recipientEmail);

		// Create the HTML body using Thymeleaf
		final String htmlContent = this.htmlTemplateEngine.process(EMAIL_INLINEIMAGE_TEMPLATE_NAME, ctx);
		message.setText(htmlContent, true /* isHtml */);

		// Add the inline image, referenced from the HTML code as "cid:${imageResourceName}"
		final InputStreamSource imageSource = new ByteArrayResource(imageBytes);
		message.addInline(imageResourceName, imageSource, imageContentType);

		// Send mail
		this.mailSender.send(mimeMessage);
	}


	/* 
	 * Send HTML mail with inline image
	 */
	public String getEditableMailTemplate() throws IOException {
		final Resource templateResource = this.applicationContext.getResource(EMAIL_EDITABLE_TEMPLATE_CLASSPATH_RES);
		final InputStream inputStream = templateResource.getInputStream();
		return IOUtils.toString(inputStream, SpringMailConfig.EMAIL_TEMPLATE_ENCODING);
	}


	/*
	 * Send HTML mail with inline image
	 */
	public void sendEditableMail(
			final String recipientName, final String recipientEmail, final String htmlContent,
			final Locale locale)
					throws MessagingException {

		// Prepare message using a Spring helper
		final MimeMessage mimeMessage = this.mailSender.createMimeMessage();
		final MimeMessageHelper message
		= new MimeMessageHelper(mimeMessage, true /* multipart */, "UTF-8");
		message.setSubject("Example editable HTML email");
		message.setFrom("thymeleaf@example.com");
		message.setTo(recipientEmail);

		// Prepare the evaluation context
		final Context ctx = new Context(locale);
		ctx.setVariable("name", recipientName);
		ctx.setVariable("subscriptionDate", new Date());
		ctx.setVariable("hobbies", Arrays.asList("Cinema", "Sports", "Music"));

		// Create the HTML body using Thymeleaf
		final String output = stringTemplateEngine.process(htmlContent, ctx);
		message.setText(output, true /* isHtml */);

		// Add the inline images, referenced from the HTML code as "cid:image-name"
		message.addInline("background", new ClassPathResource(BACKGROUND_IMAGE), PNG_MIME);
		message.addInline("logo-background", new ClassPathResource(LOGO_BACKGROUND_IMAGE), PNG_MIME);
		message.addInline("thymeleaf-banner", new ClassPathResource(THYMELEAF_BANNER_IMAGE), PNG_MIME);
		message.addInline("thymeleaf-logo", new ClassPathResource(THYMELEAF_LOGO_IMAGE), PNG_MIME);

		// Send mail
		this.mailSender.send(mimeMessage);
	}

}
