package de.hpi.bpt.chimera.execution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import de.hpi.bpt.chimera.execution.exception.IllegalCaseIdException;
import de.hpi.bpt.chimera.execution.exception.IllegalCaseModelIdException;
import de.hpi.bpt.chimera.model.CaseModel;
import de.hpi.bpt.chimera.persistencemanager.CaseModelManager;
import de.hpi.bpt.chimera.persistencemanager.DomainModelPersistenceManager;

/**
 * I manage all running cases and provide access to their {@link CaseExecutioner}s.
 * I am a static class that provides only static methods and cannot be instantiated.
 */
public final class ExecutionService {
	private static final Logger log = Logger.getLogger(ExecutionService.class);
	/**
	 * Map of CaseModelId to a list of {@link CaseExecutioner}s.
	 */
	private static Map<String, List<CaseExecutioner>> caseModelIdToCaseExecutions = new ConcurrentHashMap<>();
	/**
	 * Map of CaseId to their {@link CaseExecutioner}. These are the active cases stored in memory.
	 */
	private static Map<String, CaseExecutioner> cases = new ConcurrentHashMap<>();

	// Do not instantiate 
	private ExecutionService() {
	}

	/**
	 * Checks whether a case specified by its caseId exists. If it is not active but exists in the
	 * database, it will be loaded and added to the active cases.
	 * 
	 * @param caseId
	 * @return true when the case identified by this caseId exists, false
	 *         otherwise.
	 */
	public static boolean isExistingCase(String caseId) {
		if (cases.containsKey(caseId)) {
			return true;
		}
		Case caze = DomainModelPersistenceManager.loadCase(caseId);
		if (caze != null) {
			addCase(caze.getCaseExecutioner());
			return true;
		}
		return false;
	}

	/**
	 * Hands out the {@link CaseExecutioner} responsible for executing a case.
	 * Throws an exception if no case model exists with the given id, or no case
	 * exists with the given id.
	 * 
	 * @param cmId
	 *            - Id of the case model
	 * @param caseId
	 *            - Id of the case
	 * @return The CaseExecutioner responsible for the case
	 * @throws IllegalCaseModelIdException
	 *             if cmId is not assigned
	 * @throws IllegalCaseIdException
	 *             if caseId is not assigned
	 */
	public static CaseExecutioner getCaseExecutioner(String cmId, String caseId) {
		if (!CaseModelManager.isExistingCaseModel(cmId)) {// ||
															// !caseExecutions.containsKey(cmId))
															// {
			IllegalCaseModelIdException e = new IllegalCaseModelIdException(cmId);
			log.error(e.getMessage());
			throw e;
		}
		if (!cases.containsKey(caseId)) {
			Case caze = DomainModelPersistenceManager.loadCase(caseId);
			if (caze == null) {
				IllegalCaseIdException e = new IllegalCaseIdException(caseId);
				log.error(e.getMessage());
				throw e;
			} else {
				addCase(caze.getCaseExecutioner());
			}
		}
		return cases.get(caseId);
	}

	public static CaseExecutioner getCaseExecutioner(String caseId) {
		if (!cases.containsKey(caseId)) {
			Case caze = DomainModelPersistenceManager.loadCase(caseId);
			if (caze == null) {
				IllegalCaseIdException e = new IllegalCaseIdException(caseId);
				log.error(e.getMessage());
				throw e;
			} else {
				addCase(caze.getCaseExecutioner());
			}
		}
		return cases.get(caseId);
	}
	/**
	 * Creates a new {@link CaseExecutioner} for the case model by the given id.
	 * The case executioner is added to the list of active cases. The caller
	 * still needs to start the case.
	 * 
	 * @param cmId
	 *            - id of CaseModel to be instantiated
	 * @param name
	 *            - name for the case
	 * @return the created CaseExecutioner
	 * @throws IllegalCaseModelIdException
	 *             - if cmId is not assigned
	 * @see {@link CaseExecutioner#startCase() startCase},
	 *      {@link #createCaseExecutioner(CaseModel, String)
	 *      createCaseExecutioner from CaseModel}
	 */
	public static CaseExecutioner createCaseExecutioner(String cmId, String name) {
		try {
			CaseModel cm = CaseModelManager.getCaseModel(cmId);
			return createCaseExecutioner(cm, name);
		} catch (IllegalCaseModelIdException e) {
			throw e;
		}
	}

	/**
	 * Creates a new {@link CaseExecutioner} for the given case model. The case
	 * executioner is added to the list of active cases. The caller still needs
	 * to start the case.
	 * 
	 * @param cm
	 *            - case model to be instantiated
	 * @param name
	 *            - name for the case
	 * @return the created CaseExecutioner
	 * @see {@link CaseExecutioner#startCase() startCase}
	 */
	public static CaseExecutioner createCaseExecutioner(CaseModel cm, String name) {
		String caseName;
		if (name == null || name.isEmpty()) {
			// no name specified, use name of case model
			caseName = cm.getName();
		} else {
			caseName = name;
		}
		CaseExecutioner caseExecutioner = new CaseExecutioner(cm, caseName);
		addCase(caseExecutioner);

		log.info(String.format("Successfully created Case with Case-Id: %s", caseExecutioner.getCase().getId()));
		return caseExecutioner;
	}

	synchronized private static void addCase(CaseExecutioner caseExecutioner) {
		if (cases.containsKey(caseExecutioner.getCase().getId())) {
			return;
		}

		String caseId = caseExecutioner.getCase().getId();
		String cmId = caseExecutioner.getCaseModel().getId();
		cases.put(caseId, caseExecutioner);
		// check whether there are running CaseExecutions to the CaseModel
		if (caseModelIdToCaseExecutions.containsKey(cmId)) {
			List<CaseExecutioner> caseExecutioners = caseModelIdToCaseExecutions.get(cmId);
			caseExecutioners.add(caseExecutioner);
		} else {
			List<CaseExecutioner> caseExecutioners = new ArrayList<>();
			caseExecutioners.add(caseExecutioner);
			caseModelIdToCaseExecutions.put(cmId, caseExecutioners);
		}
	}

	/**
	 * Gets all cases of the case model by the given id. If the id is not
	 * assigned load CaseExecutioner manually from database. If there are no
	 * Cases but the request was successful return an empty list because the
	 * CaseModel was not instantiated yet. If the request fails there is no
	 * CaseModel with the specified id.
	 * 
	 * @param cmId
	 *            - the case model id
	 * @return List of all {@link CaseExecutioner}s
	 * @throws IllegalCaseModelIdException
	 *             if cmId is not assigned
	 */
	public static List<CaseExecutioner> getAllCasesOfCaseModel(String cmId) {
		// TODO talk about the "|| !caseExecutions.containsKey(cmId)". One
		// commit added it, another deleted it and a third added it again. Maybe
		// we should clarify that ;).
		if (!CaseModelManager.isExistingCaseModel(cmId) || !caseModelIdToCaseExecutions.containsKey(cmId)) {
			List<CaseExecutioner> cases = DomainModelPersistenceManager.loadAllCaseExecutionersWithCaseModelId(cmId);
			if (cases != null) {
				for (CaseExecutioner ce : cases) {
					addCase(ce);
				}
			} else {
				IllegalCaseModelIdException e = new IllegalCaseModelIdException(cmId);
				log.error(e.getMessage());
				throw e;
			}
		}
		if (!caseModelIdToCaseExecutions.containsKey(cmId)) {
			// no existing cases for existing casemodel
			return new ArrayList<>();
		}
		log.info(String.format("Successfully requested all Case-Informations of CaseModel-Id: %s", cmId));
		return caseModelIdToCaseExecutions.get(cmId).stream()
				.sorted(Comparator.comparing(CaseExecutioner::getInstantiation))
				.collect(Collectors.toList());
	}

	/**
	 * Remove all active cases for a case model.
	 * <b>The cases are not deleted in the database, they are just no longer stored in memory</b>.
	 * 
	 * @param cmId
	 */
	synchronized public static void deleteAllCasesOfCaseModel(String cmId) {
		if (caseModelIdToCaseExecutions.containsKey(cmId)) {
			List<CaseExecutioner> executions = caseModelIdToCaseExecutions.get(cmId);
			for (CaseExecutioner caseExecutioner : executions) {
				DomainModelPersistenceManager.deleteCase(caseExecutioner.getCase());
				log.info("remove Case with id:" + caseExecutioner.getCase().getId());
				cases.remove(caseExecutioner.getCase().getId());
			}
			caseModelIdToCaseExecutions.remove(cmId);
			log.info("Deleted all Cases of a CaseModel.");
		} else {
			log.info(String.format("CaseModel with id: %s is not assigned or hadn't any cases", cmId));
		}
	}
	
	/**
	 * Provides a list of {@link CaseExecutioner}s for all active cases.
	 * 
	 * @return List of all active CaseExecutioner
	 */
	public static List<CaseExecutioner> getAllExecutingCaseExecutioner() {
		return new ArrayList<>(cases.values());
	}

	public static Map<String, CaseExecutioner> getCasesMap() {
		return cases;
	}

	public static Map<String, List<CaseExecutioner>> getCaseModelIdToCaseExecutions() {
		return caseModelIdToCaseExecutions;
	}
}
