/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

//
//  @author @cpuheater
//

#include <op_boilerplate.h>
#if NOT_EXCLUDED(OP_is_strictly_increasing)

#include <ops/declarable/CustomOperations.h>
#include <ops/declarable/helpers/compare_elem.h>

namespace nd4j {
    namespace ops {
        BOOLEAN_OP_IMPL(is_strictly_increasing, 1, true) {

            auto input = INPUT_VARIABLE(0);

            bool isStrictlyIncreasing = true;

            nd4j::ops::helpers::compare_elem(input, true, isStrictlyIncreasing);

            if (isStrictlyIncreasing)
                return ND4J_STATUS_TRUE;
            else
                return ND4J_STATUS_FALSE;
        }
    }
}

#endif