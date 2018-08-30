// < begin copyright > 
// Copyright Ryan Marcus 2018
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
// 
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
// 
// < end copyright > 
package edu.brandeis.nashdb;

/**
 * Represents a transition action as party of a transition policy.
 * 
 * Each action is either:
 * <ul>
 * <li>A transition: a VM is transformed from the before state to the after state</li>
 * <li>A provision: a new VM with the fragments contained in the after state is created</li>
 * <li>A delete: an old VM with the fragments contained in the before state is deleted or removed</li>
 * </ul>
 * 
 * @author "Ryan Marcus <ryan@ryanmarc.us>"
 *
 * @param <T>
 */
public class TransitionAction<T extends FragmentHolder> {
	
	public enum ActionType {
		TRANSITION, PROVISION, DELETE;
	}
	
	private final ActionType type;
	private final T before;
	private final T after;
	
	static <K extends FragmentHolder> TransitionAction<K> transitionAction(K before, K after) {
		return new TransitionAction<K>(ActionType.TRANSITION, before, after);
	}
	
	static <K extends FragmentHolder> TransitionAction<K> provisionAction(K newFrags) {
		return new TransitionAction<K>(ActionType.PROVISION, null, newFrags);
	}
	
	static <K extends FragmentHolder> TransitionAction<K> deleteAction(K oldFrags) {
		return new TransitionAction<K>(ActionType.DELETE, oldFrags, null);
	}
	
	private TransitionAction(ActionType type, T before, T after) {
		this.type = type;
		this.before = before;
		this.after = after;
	}
	
	public boolean isTransition() { return type == ActionType.TRANSITION; }
	public boolean isProvision() { return type == ActionType.PROVISION; }
	public boolean isDelete() { return type == ActionType.DELETE; }
	public ActionType getType() { return type; }
	
	public T getAfter() { return after; }
	public T getBefore() { return before; }
	
	@Override
	public String toString() {
		switch (type) {
		case TRANSITION:
			return "<transition from: " + before + " to: " + after + ">";
		case PROVISION:
			return "<provision " + after + ">";
		case DELETE:
			return "<delete "  + before + ">";
		}
		
		throw new IllegalStateException();
	}
}
