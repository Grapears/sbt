<script setup lang="ts">
import {ref} from 'vue'
import VLoginTitle from '@/components/molecules/VLoginTitle.vue'
import RegisterForm from '@/components/molecules/RegisterForm.vue'
import { useCheckUserStore } from '@/stores/checkUser'
import axiosInstance from '@/axiosInstance';
import { useAuthenticationStore } from '@/stores/authentication'
import { googleSdkLoaded } from 'vue3-google-login'
import {storeToRefs} from 'pinia'
import { toast } from 'vue3-toastify';

const { checkUser, fetchUser } = useCheckUserStore()
const userStore = useAuthenticationStore()
const { fetchUserDataFrom, logoutGoogle, isGoogleUserRegistered } = userStore
const { clientId } = storeToRefs(userStore)

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const errors = ref('');
const success = ref('');
const regexEmail = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;
const regexPassword= /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
const emit = defineEmits(['register-user']);

const registerUser = async (username: string, password: string, confirmPassword: string) => {
    
    if (!regexEmail.test(username)) {
        return;
    }

    if (password !== confirmPassword) {
        return;
    }

    const userData = {
        emailUser: username,
        passwordUser: password
    };


    try {
          const response = await axiosInstance.post('http://localhost:9000/api/users/register', userData);
          const data = await response.data;

          if (response.status === 200){
            success.value = 'Successful Registration!'
            await fetchUser()
            errors.value = ''
            toast.success("Successful Registration!", {
              position: 'bottom-right',
              autoClose: 1500,
            });
            setTimeout(() => {
              window.location.href = '/login';
            }, 1500);
          }

    } catch (error: any) {
        errors.value = error.response.data.error;
        success.value = ''
        toast.error(errors.value, {
          position: 'bottom-right',
          autoClose: 3000,
        });
    }



}

const signInWithGoogle1 = async () => {
  const isRegistered = await isGoogleUserRegistered();
  if (isRegistered) {
    window.alert("User already registered");
  } else {
    googleSdkLoaded(google => {
      google.accounts.oauth2
        .initCodeClient({
          client_id: clientId.value,
          scope: 'email profile openid',
          redirect_uri: "http://localhost:5173/",
          callback: response => {
            if (response.code)
              fetchUserDataFrom(response.code)
          },
        })
        .requestCode()
    })
  }
};

const signInWithGoogle = async () => {
  googleSdkLoaded(google => {
    google.accounts.oauth2
      .initCodeClient({
        client_id: clientId.value,
        scope: 'email profile openid',
        redirect_uri: "http://localhost:5173/",
        callback: response => {
          if (response.code)
            fetchUserDataFrom(response.code)
        },
      })
      .requestCode()
  })
};

</script>

<template>
  <div class="login-organisms">
    <div class="left-line">
      <VLoginTitle />
    </div>
    <div class="right-line">
      <RegisterForm :errors="errors" :success="success" @click="registerUser" @googleClick="signInWithGoogle"/>
    </div>

  </div>
</template>

<style scoped>
*{
  /* outline: 1px solid black; */
}
.login-organisms{
  margin-top: 100px;
  margin-bottom: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.right-line{
  border-left: 1px solid black;
  padding: 0 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.left-line{
  padding: 0 100px;
}

.errors {
  color: red;
}

@media(max-width: 970px){
  .login-organisms{
    flex-direction: column;
  }
  .left-line{
    margin-bottom: 1em;
  }
  .right-line{
    border-left: none;
  }
}
</style>
_________________________________-
<script setup lang="ts">
  import Logout from '@/components/organisms/Logout.vue'
  import ChecklistCard from '@/components/molecules/ChecklistCard.vue'
  import { useChecklistStore } from '@/stores/checklist'

  const emits = defineEmits<{
    (e: 'checklistOnClickHandler', index: number): void
  }>()
  const checklistStore = useChecklistStore()
  const userEmail = localStorage.getItem('emailUser')
</script>
<template>
  <div class="menu border-bottom-1">
    <div class="d-flex space-between align-items-center p-1 border-bottom-1">
            <p>{{userEmail}}</p>
            <Logout />
        </div>
        <div class="overflow-y-auto h-40">
          <ChecklistCard
            v-for="(checklist, index) in checklistStore.checklists"
              :key="index"
              :checklist
              :index
              @checklistOnClick="emits('checklistOnClickHandler', index)"
          />
      </div>
  </div>
</template>
<style scoped>
  .menu{
    position: absolute;
    width: 100%;
    top: 70px;
    background-color: var(--color1);
    z-index: 2;
  }
  .menu::after{
    content: '';
    width: 100%;
    height: 5px;
    position: absolute;
    background-color: var(--color2);
  }
</style>

_________________________________
<script setup lang="ts">

import { useRouter } from 'vue-router';
import axiosInstance from '@/axiosInstance'
import { useRouteStore } from '@/stores/route'
import { useAuthenticationStore } from '@/stores/authentication'
import { toast } from 'vue3-toastify';
const routeStore = useRouteStore()

import { useChecklistItemStore } from '@/stores/checklistItem'

const emit = defineEmits(['logout-user']);
const router = useRouter();

const checklistItemStore = useChecklistItemStore()
const userStore = useAuthenticationStore()
const { fetchUserDataFrom, logoutGoogle, isGoogleUserRegistered } = userStore

const logoutUser = async (event: Event) => {
    event.preventDefault();

    try {
        const response = await axiosInstance.post('users/logout')
        const data = response.data

        if (response.status === 200) {
            if(data.message == "success"){
                localStorage.removeItem('idUser')
                localStorage.removeItem('emailUser')
                checklistItemStore.checklistItems = []
                await logoutGoogle()
                  toast.success('Logged out successfully!', {
                    position: 'bottom-right',
                    autoClose: 1500,
                  });
                  setTimeout(() => {
                    window.location.href = '/';
                  }, 1500);
            }
        } else {
            const errorData = response
        }
    } catch (error) {
        console.error("Error during logout:", error);
    }
}
</script>

<template>
    <button 
        class="button-2 logout-btn"
        @click="logoutUser"
    >
        <img class="logout-img" src="@/assets/logout.svg" width=30 height=30>
    </button>
</template>
-----------------------------------
<script setup lang="ts">
import {ref} from 'vue'
import VLoginTitle from '@/components/molecules/VLoginTitle.vue'
import VLoginForm from '@/components/molecules/VLoginForm.vue'
import { useCheckUserStore } from '@/stores/checkUser'
import axiosInstance from '@/axiosInstance';
import { useAuthenticationStore } from '@/stores/authentication'
import { googleSdkLoaded } from 'vue3-google-login'
import {storeToRefs} from 'pinia'
import { toast } from 'vue3-toastify';

const { checkUser, fetchUser } = useCheckUserStore()
const userStore = useAuthenticationStore()
const { fetchUserDataFrom, logoutGoogle, isGoogleUserRegistered, loginGoogle } = userStore
const { clientId } = storeToRefs(userStore)

const loginUser = ref('');
const password = ref('');
const errors = ref('');
const success = ref('');
const regexEmail = /^[\w-\.]+@([\w-]+\.)+[\w-]{2,4}$/;

const userLogin = async (email: string, password: string) => {
    if (!regexEmail.test(email)) {
        return;
    }

    const userData = {
        emailUser: email,
        passwordUser: password
    };

    try {
        const response = await axiosInstance.post('http://localhost:9000/api/users/login', userData);
        const data = await response.data;

        if (response.status === 200) {
            success.value = 'Successful Login!'
            localStorage.setItem('idUser', data.data.idUser)
            localStorage.setItem('emailUser', data.data.emailUser)
            await fetchUser()
            errors.value = ''
            toast.success(success.value, {
              position: 'bottom-right',
              autoClose: 800,
            });
            setTimeout(() => {
              window.location.href = '/';
            }, 1500);
        }
        
    } catch (error) {
        success.value = ''
        errors.value = "Invalid Credentials"
        toast.error("Invalid Credentials", {
          position: 'bottom-right',
          autoClose: 3000,
        });
    }
}

const signInWithGoogle1 = async () => {
  const isRegistered = await isGoogleUserRegistered();
  if (isRegistered) {
    window.alert("User already registered");
  } else {
    googleSdkLoaded(google => {
      google.accounts.oauth2
        .initCodeClient({
          client_id: clientId.value,
          scope: 'email profile openid',
          redirect_uri: "http://localhost:5173/",
          callback: response => {
            if (response.code)
              fetchUserDataFrom(response.code)
          },
        })
        .requestCode()
    })
  }
};

const signInWithGoogle = async () => {
  googleSdkLoaded(google => {
      google.accounts.oauth2
        .initCodeClient({
          client_id: clientId.value,
          scope: 'email profile openid',
          redirect_uri: "http://localhost:5173/",
          callback: response => {
            if (response.code)
              loginGoogle(response.code)
          },
        })
        .requestCode()
    })
};

</script>

<template>
  <div class="login-organisms">
    <div class="left-line">
      <VLoginTitle />
    </div>
    <div class="right-line">
      <VLoginForm 
        :errors 
        :success 
        @click="userLogin" 
        @googleClick="signInWithGoogle"
      />
    </div>
  </div>
</template>

<style scoped>
.login-organisms{
  margin-top: 100px;
  margin-bottom: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
}
.right-line{
  border-left: 1px solid black;
  padding: 0 100px;
  height: 400px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.left-line{
  padding: 0 100px;
}

@media(max-width: 970px){
  .login-organisms{
    flex-direction: column;
  }
  .right-line{
    border-left: none;
  }
}

</style>

------------------------------------------    <script setup lang="ts">
    import { ref, onUnmounted } from 'vue'
    import ChecklistItem from '@/components/molecules/ChecklistItem.vue'
    import Modal from '@/components/molecules/Modal.vue'
    import { useModalStore } from '@/stores/modal'
    import { useChecklistItemStore } from '@/stores/checklistItem'
    import { useChecklistStore } from '@/stores/checklist'
    import { useMembersStore } from '@/stores/members'
    import { useDashboardStore } from '@/stores/dashboard'
    // import type { ChecklistItem as ChecklistItemType } from '@/stores/checklistItem'
    import { toast } from 'vue3-toastify';
    interface ChecklistItem {
      idChecklistItem: string;
      checklistsIdChecklistItem: string;
      parentIdChecklistItem?: string;
      descriptionChecklistItem: string;
      isCheckedChecklistItem: boolean;
      levelChecklistItem: number;
    }

    const modalStore = useModalStore()
    const checklistItemStore = useChecklistItemStore()
    const checklistStore = useChecklistStore()
    const membersStore = useMembersStore()
    const dashboardStore = useDashboardStore()

    const props = defineProps<{
        checklistItemsRef: ChecklistItem[] | (() => ChecklistItem[])
    }>()

    const checklistIdRef = ref<string | undefined>(undefined)
    const checklistItemIdRef = ref<string | undefined>(undefined)
    const parentIdRef = ref<string | undefined>(undefined)
    const parentLevelRef = ref<number | undefined>(undefined)
    const descriptionRef = ref<string | undefined>(undefined)

    const { 
            addChecklistItem, 
            getChecklistItemsByChecklistId, 
            updateChecklistItemDescription,
            deleteChecklistItem 
        } = checklistItemStore
    const { updateChecklistName, deleteChecklist } = checklistStore
    const { deleteMembersByChecklistId } = membersStore

    async function addChecklistItemHandler(description: string): Promise<void> {
        if (checklistIdRef.value && parentLevelRef.value !== undefined) {
            await addChecklistItem(
                checklistIdRef.value,
                description,
                parentLevelRef.value,
                parentIdRef.value
            )
            // toast.success('Added new checklist task successfully!', {
            //   position: 'bottom-right',
            //   autoClose: 2000,
            // });
            modalStore.showAddChecklistItemModalRef = false
        }
    }

    async function updateChecklistItemHandler(description: string): Promise<void> {
        await updateChecklistItemDescription(checklistItemIdRef.value!, checklistIdRef.value!, description)
        if(!parentIdRef.value) await updateChecklistName(checklistIdRef.value!, description)
        // toast.success('Updated checklist task name successfully!', {
        //   position: 'bottom-right',
        //   autoClose: 2000,
        // });
        modalStore.showUpdateChecklistItemModalRef = false
        modalStore.showUpdateChecklistItemModalRef1.show = false
    }

    async function deleteChecklistItemHandler(): Promise<void> {
        await deleteChecklistItem(checklistItemIdRef.value!, checklistIdRef.value!)
        if(!parentIdRef.value){
            await deleteMembersByChecklistId(checklistIdRef.value!)
            await deleteChecklist(checklistIdRef.value!)
            membersStore.membersRef = []
        } 
        // toast.success('Deleted checklist task successfully!', {
        //   position: 'bottom-right',
        //   autoClose: 2000,
        // });
        modalStore.showDeleteChecklistItemModalRef = false   
        // window.location.href = '/'
    }


    function addChecklistItemAttemptHandler(checklistId: string, parentId: string | undefined, parentLevel: number): void {
        checklistIdRef.value = checklistId
        parentIdRef.value = parentId
        parentLevelRef.value = parentLevel
        modalStore.showAddChecklistItemModalRef = true
    }
    function updateChecklistItemAttemptHandler(checklistItemId: string, checklistId: string, parentIdChecklistItem: string | undefined, descriptionChecklistItem: string | undefined): void {
        checklistItemIdRef.value = checklistItemId
        parentIdRef.value = parentIdChecklistItem
        checklistIdRef.value = checklistId
        modalStore.showUpdateChecklistItemModalRef = true
        modalStore.showUpdateChecklistItemModalRef1 = {
            show: true,
            data: descriptionChecklistItem!
        }
    }
    function deleteChecklistItemAttemptHandler(checklistItemId: string, checklistsIdChecklistItem: string, parentIdChecklistItem?: string, descriptionChecklistItem?: string): void {

        checklistItemIdRef.value = checklistItemId
        checklistIdRef.value = checklistsIdChecklistItem
        parentIdRef.value = parentIdChecklistItem
        descriptionRef.value = descriptionChecklistItem
        modalStore.showDeleteChecklistItemModalRef = true
    }

    onUnmounted(() => {
        dashboardStore.showChecklistContentRef = false
    })
</script>

<template>
    <div class="flex-col gap-05">
        <ChecklistItem 
            v-for="(item, index) in checklistItemsRef"
            :key="index"
            :checklistItem="item"
            @addChecklistItemAttemptHandler="addChecklistItemAttemptHandler"
            @updateChecklistItemAttemptHandler="updateChecklistItemAttemptHandler"
            @deleteChecklistItemAttemptHandler="deleteChecklistItemAttemptHandler"
        />
    </div>
    <Teleport to="body">
        <Modal 
            v-if="modalStore.showAddChecklistItemModalRef"
            :isAddChecklistItem=true
            @addChecklistItem="addChecklistItemHandler"
            @closeModal="modalStore.showAddChecklistItemModalRef = false"
        />
        <Modal 
            v-else-if="modalStore.showUpdateChecklistItemModalRef1.show"
            :isUpdateChecklistItem="modalStore.showUpdateChecklistItemModalRef1"
            @updateChecklistItem="updateChecklistItemHandler"
            @closeModal="modalStore.showUpdateChecklistItemModalRef1.show = false"
        />
        <Modal 
            v-else-if="modalStore.showDeleteChecklistItemModalRef"
            :isDeleteChecklistItem=true
            :descriptionRef
            @deleteChecklistItem="deleteChecklistItemHandler"
            @closeModal="modalStore.showDeleteChecklistItemModalRef = false"
        />
    </Teleport>
</template>

---------------------------
