package people;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleService.People;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;

public class PeopleApp {

	public static People people;
	
	public static void listContacts() throws IOException {
		
		people.connections()
			.list("people/me")
			.setPageSize(10)
			.setSortOrder("FIRST_NAME_ASCENDING")
			.setPersonFields("names,phoneNumbers,emailAddresses")
			.execute()
			.getConnections()
			.forEach(PeopleApp::printPerson);
	}
	
	public static Person getContact(String resourceName) throws IOException {
		return people.getBatchGet()
				.setResourceNames(Arrays.asList(resourceName))
				.setPersonFields("names,phoneNumbers,emailAddresses")
				.execute()
				.getResponses()
				.get(0)
				.getPerson();
	}
	
	public static void createContact() throws IOException {
		
		Person person = new Person();
		
		Name name = new Name();
		name.setGivenName("Firstname");
		name.setFamilyName("Lastname");
		person.setNames(Arrays.asList(name));
		
		PhoneNumber phoneNumber = new PhoneNumber();
		phoneNumber.setType("Work");
		phoneNumber.setValue("+258801234567");
		person.setPhoneNumbers(Arrays.asList(phoneNumber));
		
		EmailAddress emailAddress = new EmailAddress();
		emailAddress.setType("Work");
		emailAddress.setValue("user@domain.com");
		person.setEmailAddresses(Arrays.asList(emailAddress));
		
		people.createContact(person).execute();
		
		System.out.println("Done");
	}

	public static void updateContact() throws IOException {
		
		String resourceName = "people/c636828715667820859";
		Person person = getContact(resourceName);

		PhoneNumber phoneNumber1 = new PhoneNumber();
		phoneNumber1.setType("Work");
		phoneNumber1.setValue("+258807654321");

		PhoneNumber phoneNumber2 = new PhoneNumber();
		phoneNumber2.setType("Home");
		phoneNumber2.setValue("+258801234567");

		person.setPhoneNumbers(Arrays.asList(phoneNumber1, phoneNumber2));

		people.updateContact(resourceName, person)
			.setUpdatePersonFields("phoneNumbers")
			.execute();

		System.out.println("Done!");
	}
	
	public static void deleteContact(String resourceName) throws IOException {
		people.deleteContact(resourceName).execute();
		System.out.println("Done");
	}
	
	public static void printPerson(Person person) {
		List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
		List<EmailAddress> emailAddresses = person.getEmailAddresses();

		System.out.println("Name: " + person.getNames().get(0).getDisplayName());
		System.out.println("Resource: " + person.getResourceName());

		if (phoneNumbers != null && phoneNumbers.size() > 0) {
			System.out.print("Phone: ");
			phoneNumbers.forEach(phoneNumber -> System.out.print(phoneNumber.getCanonicalForm() + " "));
			System.out.println("");
		}

		if (emailAddresses != null && emailAddresses.size() > 0) {
			System.out.print("E-Mail: ");
			emailAddresses.forEach(emailAddress -> System.out.print(emailAddress.getValue() + " "));
			System.out.println("");
		}

		System.out.println("");
	}

	public static void main(String[] args) throws GeneralSecurityException, IOException {

		// Build a new authorized API client service.
		NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
		InputStream in = PeopleApp.class.getResourceAsStream("/credentials.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
		List<String> scopes = Arrays.asList(PeopleServiceScopes.CONTACTS);

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory,
				clientSecrets, scopes)
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
				.setAccessType("offline")
				.build();

		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

		people = new PeopleService.Builder(httpTransport, jsonFactory, credential)
				.setApplicationName("Application name")
				.build()
				.people();
		
		//	List contacts
			listContacts();
		
		//	Get and then print contact
		//	printPerson(getContact("people/c636828714667820353"));
		
		//	Create contact
		//	createContact();
		
		//	Update contact
		//	updateContact();
		
		//	Delete contact
		//	deleteContact("people/c4035337678563471853");
	}

}
