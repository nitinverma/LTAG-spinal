/**
 * LTAG-spinal API, an interface to the treebank format introduced by Libin Shen.
 * Copyright (C) 2007  Lucas Champollion
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package edu.upenn.cis.spinal;

/**
 * Exception thrown by the <code>ElemTree</code> class when various methods
 * are called on it before it is completed.
 * 
 * @author Lucas Champollion
 * @author Ryan Gabbard
 */
public class UncompletedElemTreeException extends RuntimeException {
	
	public UncompletedElemTreeException() {
		super("ElemTrees should be completed before being used.");
	}
}