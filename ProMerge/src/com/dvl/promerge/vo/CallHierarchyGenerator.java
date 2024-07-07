package com.dvl.promerge.vo;

// http://www.programcreek.com/2011/07/find-all-callers-of-a-method/
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import com.dvl.core.vos.MetodosVO;

public class CallHierarchyGenerator {

	public HashSet<IMethod> getCallersOf(IMethod m) {

		CallHierarchy callHierarchy = CallHierarchy.getDefault();

		IMember[] members = { m };

		MethodWrapper[] methodWrappers = callHierarchy.getCallerRoots(members);
		HashSet<IMethod> callers = new HashSet<IMethod>();
		for (MethodWrapper mw : methodWrappers) {
			MethodWrapper[] mw2 = mw.getCalls(new NullProgressMonitor());
			HashSet<IMethod> temp = getIMethods(mw2);
			callers.addAll(temp);
		}

		return callers;
	}

	HashSet<IMethod> getIMethods(MethodWrapper[] methodWrappers) {
		HashSet<IMethod> c = new HashSet<IMethod>();
		for (MethodWrapper m : methodWrappers) {
			IMethod im = getIMethodFromMethodWrapper(m);
			if (im != null) {
				c.add(im);
			}
		}
		return c;
	}

	IMethod getIMethodFromMethodWrapper(MethodWrapper m) {
		try {
			IMember im = m.getMember();
			if (im.getElementType() == IJavaElement.METHOD) {
				return (IMethod) m.getMember();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static IMethod findMethod(String projeto, String classe,
			String methodName) throws JavaModelException {

		IType iType = getJavaProject(projeto).findType(classe);	

		IMethod theMethod = null;			
		IMethod[] methods = iType == null ? new IMethod[0] : iType.getMethods();

		for (int i = 0; i < methods.length; i++) {
			IMethod imethod = methods[i];
			if (imethod.getElementName().equals(methodName)) {
				theMethod = imethod;
			}
		}

		if (theMethod == null) {
			System.out.println("Error, method" + methodName + " not found");
			return null;
		}

		return theMethod;
	}

	private static IJavaProject getJavaProject(String projectName) {

		try {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject project = root.getProject(projectName);
			
			if (!project.exists()) {
				project.create(null);

			} else {
				//project.refreshLocal(IResource.DEPTH_INFINITE, null);
			}

			if (!project.isOpen()) {
				project.open(null);
			}

			IJavaProject jproject = JavaCore.create(project);

			return jproject;

		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static List<MetodosVO> buscarHierarquiaChamada(String projeto,
													      String nomeMetodo, 
													      String nomeClasse) throws JavaModelException 
    {
		CallHierarchyGenerator callGen = new CallHierarchyGenerator();
		IMethod m = findMethod(projeto, nomeClasse, nomeMetodo);

		List<MetodosVO> listaMetodos = new ArrayList<MetodosVO>(0);
		Set<IMethod> methods = new HashSet<IMethod>();
		methods = callGen.getCallersOf(m);
		
		for (Iterator<IMethod> i = methods.iterator(); i.hasNext();) {

			IMethod next = i.next();		
			
			MetodosVO metodoVO = new MetodosVO();
			String nomeCompleto = next.getPath().toString();
			
			if (nomeCompleto == null || nomeCompleto.isEmpty()) {
				continue;
			}
			
			///Sample2/src/com/Chamada1.java
			String aux = nomeCompleto.substring(1, nomeCompleto.length());
			aux = aux.replaceAll("/", "\\\\");
			
			metodoVO.setNomeClasse(aux);
			metodoVO.setNomeMetodo(next.getElementName());
			
			listaMetodos.add(metodoVO);
			
		}
		
		return listaMetodos;
	}
	
}