package de.hpi.bpt.chimera.usermanagement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.controlnodes.activity.AbstractActivityInstance;
import de.hpi.bpt.chimera.execution.exception.IllegalUserIdException;
import de.hpi.bpt.chimera.model.fragment.bpmn.activity.AbstractActivity;
import de.hpi.bpt.chimera.model.fragment.bpmn.activity.HumanTask;
import de.hpi.bpt.chimera.persistencemanager.DomainModelPersistenceManager;

public class UserManager {
	private static Logger log = Logger.getLogger(UserManager.class);
	private static Map<String, User> users = new HashMap<>();

	private static final String ADMIN_NAME = "admin";
	private UserManager() {
	}

	/**
	 * Authenticate a user with a given email and a password.
	 * 
	 * @param email
	 *            - of the user
	 * @param password
	 *            - of the user
	 * @return the authenticated user
	 * @throws IllegalArgumentException
	 *             if the email is not assigned or the password is wrong.
	 */
	public static User authenticateUser(String email, String password) {
		String hashedPassword = hashPassword(password);
		for (User user : users.values()) {
			if (user.getEmail().equals(email)) {
				if (user.getPassword().equals(hashedPassword)) {
					return user;
				} else {
					throw new IllegalArgumentException("Wrong password");
				}
			}
		}
		throw new IllegalArgumentException("Email not assigned");
	}

	/**
	 * Create a user with a specific name and assign it to the default
	 * organization.
	 * 
	 * @param email
	 * @param password
	 * @param username
	 */
	public static User createUser(String email, String password, String username) {
		// TODO: validate email, password, username
		if (email.equals(ADMIN_NAME)) {
			throw new IllegalArgumentException("This name cannot be used");
		}
		User user = new User();
		user.setEmail(email);
		String hashedPassword = hashPassword(password);
		user.setPassword(hashedPassword);
		user.setName(username);

		OrganizationManager.assignMember(OrganizationManager.getDefaultOrganization(), user);
		String id = user.getId();
		users.put(id, user);

		log.info(String.format("Created user with id %s and name %s", id, user.getName()));
		return user;
	}

	/**
	 * Assign a new email and a new password to a a user.
	 * 
	 * @param user
	 * @param newEmail
	 * @param newPassword
	 */
	public static void updateUser(User user, String newEmail, String newPassword) {
		// TODO: validate email, password
		user.setEmail(newEmail);
		user.setPassword(hashPassword(newPassword));
	}

	private static String hashPassword(String password) {
		// TODO: think about salt for the hashing
		return String.valueOf(Objects.hashCode(password));
	}

	/**
	 * Get a {@link User} by its id.
	 * 
	 * @param userId
	 * @return User
	 * @throws IllegalArgumentException
	 *             if the user is not assigned.
	 */
	public static User getUserById(String userId) {
		if (users.containsKey(userId)) {
			return users.get(userId);
		} else {
			User user = DomainModelPersistenceManager.loadUser(userId);
			if (user == null) {
				throw new IllegalUserIdException(userId);
			}
			users.put(userId, user);
			return user;
		}
	}

	/**
	 * Delete an user. Therefore, delete the memberships in the organizations.
	 * An user can only be deleted if it the only owner of one organization.
	 * 
	 * @param user
	 *            - the user to be deleted
	 */
	public static void deleteUser(User user) {
		try {
			String userId = user.getId();
			String name = user.getName();

			for (Organization org : user.getOrganizations()) {
				if (org.isSoleOwner(user)) {
					String message = String.format("The user with id %s is the last owner of the organiazion with id %s and can thus not be deleted", userId, org.getId());
					throw new IllegalArgumentException(message);
				}
			}

			for (Organization org : user.getOrganizations()) {
				OrganizationManager.removeMember(org, user);
			}

			users.remove(user.getId());
			DomainModelPersistenceManager.removeUser(user);
			log.info(String.format("Deleted user with id %s and name %s", user.getId(), name));
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Remove a specific {@link MemberRole} of a user in an organization.
	 * 
	 * @param user
	 *            - whose role will me removed
	 * @param org
	 *            - organization where the users role will be removed
	 * @param role
	 *            - to be removed
	 */
	public static void deleteRole(User user, Organization org, MemberRole role) {
		if (!org.isMember(user)) {
			throw new IllegalArgumentException("The specified user is not a member of the organization");
		}
		List<MemberRole> roles = org.getMemberRoles(user);
		roles.remove(role);
	}

	/**
	 * Decide whether a user can access a certain
	 * {@link AbstractActivityInstance} with roles provided by an
	 * {@link Organization}. Only {@link HumanTask} has a defined role. An owner
	 * has access to all activities in an organization. If the role of the task
	 * is {@code member} every member of the organization has access to it.
	 * Otherwise the {@link User} needs to have the correct {@link MemberRole}
	 * in the organization.
	 * 
	 * @param user
	 * @param org
	 * @param activityInstance
	 * @return boolean whether a user can access a certain Activity Instance
	 *         with roles provided by the Organization.
	 */
	public static boolean hasAccess(User user, Organization org, AbstractActivityInstance activityInstance) {
		if (!org.isMember(user)) {
			return false;
		}
		if (org.isOwner(user)) {
			return true;
		}

		AbstractActivity activity = activityInstance.getControlNode();
		if (!(activity instanceof HumanTask)) {
			return true;
		}
		HumanTask task = (HumanTask) activity;
		String role = task.getRole();

		if ("member".equals(role)) {
			return true;
		}
		List<MemberRole> memberRoles = org.getMemberRoles(user);
		Predicate<MemberRole> hasRole = r -> r.getName().equals(role);

		return memberRoles.stream().anyMatch(hasRole);
	}

	/**
	 * Create an admin for the system who will be the owner of the default
	 * organization.
	 */
	public static User createAdmin() {
		for (User user : users.values()) {
			if (ADMIN_NAME.equals(user.getEmail())) {
				return user;
			}
		}
		// TODO: create User by properties
		// TODO: validate email, password, username
		String email = ADMIN_NAME;
		String password = "admin";
		String username = ADMIN_NAME;

		User admin = new User();
		admin.setEmail(email);
		String hashedPassword = hashPassword(password);
		admin.setPassword(hashedPassword);
		admin.setName(username);
		admin.getSystemRoles().add(SystemRole.ADMIN);

		log.info("Admin created");
		String id = admin.getId();
		users.put(id, admin);
		return admin;
	}

	public static List<User> getUsers() {
		return new ArrayList<>(users.values());
	}

	public static Map<String, User> getUsersMap() {
		return users;
	}

	public static void setUsers(List<User> newUsers) {
		users = new HashMap<>();
		for (User user : newUsers) {
			users.put(user.getId(), user);
		}
	}
}
