/*
 * Copyright (C) 2018 Tran Le Duy
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.duy.file.explorer;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class ExplorerException extends RuntimeException {
    public ExplorerException(String detailMessage) {
        super(detailMessage);
    }

    public ExplorerException(Throwable throwable) {
        super(throwable);
    }

    public ExplorerException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ExplorerException() {
    }
}
