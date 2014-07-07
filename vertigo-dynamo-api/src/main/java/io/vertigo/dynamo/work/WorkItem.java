package io.vertigo.dynamo.work;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.UUID;
import java.util.concurrent.Callable;

public final class WorkItem<WR, W> {
	//private static final Object DUMMY_WORK = new Object();

	//	private static enum Status {
	//		Waiting, InProgress, Succeeded, Failed
	//	}

	private final W work;
	private final Option<WorkResultHandler<WR>> workResultHandler;
	private final WorkEngineProvider<WR, W> workEngineProvider;
	private WR result;
	final String id = UUID.randomUUID().toString();

	//	private Status status = Status.Waiting;

	public WorkItem(final Callable<WR> callable, final WorkResultHandler<WR> workResultHandler) {
		Assertion.checkNotNull(callable);
		Assertion.checkNotNull(workResultHandler);
		//---------------------------------------------------------------------
		this.work = null;
		this.workResultHandler = Option.some(workResultHandler);
		this.workEngineProvider = new WorkEngineProvider<>(new AsyncEngine<WR, W>(callable));
	}

	/**
	 * Constructeur.
	 * @param work Travail dont on représente l'état.
	 */
	public WorkItem(final W work, final WorkEngineProvider<WR, W> workEngineProvider) {
		Assertion.checkNotNull(work);
		Assertion.checkNotNull(workEngineProvider);
		//---------------------------------------------------------------------
		this.work = work;
		this.workResultHandler = Option.none();
		this.workEngineProvider = workEngineProvider;
	}

	/**
	 * Constructeur.
	 * @param work Travail dont on représente l'état.
	 */
	public WorkItem(final W work, final WorkEngineProvider<WR, W> workEngineProvider, final WorkResultHandler<WR> workResultHandler) {
		Assertion.checkNotNull(work);
		Assertion.checkNotNull(workEngineProvider);
		Assertion.checkNotNull(workResultHandler);
		//---------------------------------------------------------------------
		this.work = work;
		this.workResultHandler = Option.some(workResultHandler);
		this.workEngineProvider = workEngineProvider;
	}

	public String getId() {
		return id;
	}

	/**
	 * Permet de récupérer les informations pour réaliser un traitement. 
	 * @return le work
	 */
	public W getWork() {
		return work;
	}

	/**
	 * Permet de récupérer le WorkResultHandler traitant les resultats de l'éxecution. 
	 * @return le work
	 */
	public Option<WorkResultHandler<WR>> getWorkResultHandler() {
		return workResultHandler;
	}

	public WorkEngineProvider<WR, W> getWorkEngineProvider() {
		return workEngineProvider;
	}

	public synchronized WR getResult() {
		return result;
	}

	public synchronized void setResult(WR newResult) {
		Assertion.checkNotNull(newResult);
		//---------------------------------------------------------------------
		this.result = newResult;
	}
}