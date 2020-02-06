/*******************************************************************************
 * PLA Adaptation Manager
 *
 * Copyright 2017 Carnegie Mellon University. All Rights Reserved.
 * 
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED, AS
 * TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE
 * MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF ANY KIND
 * WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * Released under a BSD-style license, please see license.txt or contact
 * permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public release
 * and unlimited distribution. Please see Copyright notice for non-US Government
 * use and distribution.
 ******************************************************************************/
package reach;

import java.util.ArrayList;

public class DynamicMatrix<T> {
	public ArrayList<ArrayList<T> > matrix;
	
	public DynamicMatrix() {
		matrix = new ArrayList<>();
	}
	
	public T get(int row, int column) {
		T value = null;
		if (row < matrix.size()) {
			ArrayList<T> columns = matrix.get(row);
			if (column < columns.size()) {
				value = columns.get(column);
			}
		}
		
		return value;
	}

	public void set(int row, int column, T value) {
		if (row >= matrix.size()) {
			matrix.ensureCapacity(row + 1);
			while (matrix.size() < row + 1) {
				matrix.add(new ArrayList<T>());
			}
		}
		ArrayList<T> columns = matrix.get(row);
		if (column >= columns.size()) {
			columns.ensureCapacity(column + 1);
			while (columns.size() < column + 1) {
				columns.add(null);
			}
		}
		columns.set(column, value);
	}

}
