import { ref } from "vue";
import { defineStore } from "pinia";
import axiosInstance from '@/axiosInstance'


export const useCheckUserStore = defineStore("checkUser", () => {
  const checkUser = ref(null);

  const fetchUser = async () => {
    try {
      const response = await axiosInstance('users/user')
      const data = response.data
      localStorage.setItem("emailUser", data.data?.emailUser)
      localStorage.setItem("idUser", data.data?.idUser)
      checkUser.value = data.data
    } catch (error) {
      console.error(error)
    }
  }

  return {
    checkUser,
    fetchUser
  }
}) 
-----------
import { ref } from 'vue'
import { defineStore } from 'pinia'

interface User{
  idUser: string
  emailUser: string
}

export const useUserStore = defineStore('user', () => {
  const usersRef = ref<User[]>([])
  const responseMessageRef = ref("")

  async function getUsers(){ 
    await fetch('http://localhost:9000/api/users/users')
      .then(res => res.json())
      .then(data => usersRef.value = data)
      .catch(error => responseMessageRef.value = error)
  }

  async function getUserIdByUserEmail(email: string): Promise<User | null>{
    let userId
    await fetch(`http://localhost:9000/api/users/id/${email}`)
      .then(res => res.json())
      .then(data => userId = data)
      .catch(error => responseMessageRef.value = error)

    return userId!
  }

  async function getUserEmailById(userId: string): Promise<string> {
    let userEmail
    await fetch(`http://localhost:9000/api/users/email/${userId}`)
      .then(res => res.json())
      .then(data => userEmail = data.data)
      .catch(error => responseMessageRef.value = error)

    return userEmail!
  }

  return {
    usersRef,
    getUsers,
    getUserIdByUserEmail,
    getUserEmailById,
    responseMessageRef
  }
})
----------------
import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useRouteStore = defineStore('route', () => {
  const currentLocationRef = ref()
  return {
    currentLocationRef
  }
})
-----------------
import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useModalStore = defineStore('modal', () => {
  const showAddChecklistModalRef = ref(false)
  const showAddChecklistItemModalRef = ref(false)
  const showUpdateChecklistItemModalRef = ref(false)
  const showUpdateChecklistItemModalRef1 = ref({
    show: false,
    data: ""
  })
  const showDeleteChecklistItemModalRef = ref(false)
  const showAddMemberModalRef = ref(false)
  return { 
    showAddChecklistModalRef,
    showAddChecklistItemModalRef,
    showUpdateChecklistItemModalRef,
    showUpdateChecklistItemModalRef1,
    showDeleteChecklistItemModalRef,
    showAddMemberModalRef
  }
})
-----------
import { ref } from 'vue'
import { defineStore } from 'pinia'
import axiosInstance from '@/axiosInstance';
import { useChecklistStore } from '@/stores/checklist'

interface Members{
  idUser: string
  userEmailMember: string
}

interface MembersE{
  idUser: string
  emailUser: string
}

interface Checklist {
  idChecklist: string;
  nameChecklist: string;
  idUserChecklist: string;
  isCheckedChecklist: boolean;
  createdAtChecklist: number;
}


export const useMembersStore = defineStore('members', () => {
  const membersRef = ref<Members[]>([]);
  const membersRefExceptOne = ref<MembersE[]>([]);
  const errorRef = ref('');
  const responseMessageRef = ref('')
  const checklistStore = useChecklistStore()

  async function fetchMembersExceptMe() {
    try {
      const response = await axiosInstance.get('users/users/exceptOne');
      if (response.data.message === 'success') {
        membersRefExceptOne.value = response.data.data;
      } else {
        errorRef.value = response.data.error;
      }
    } catch (err) {
      errorRef.value = 'Failed to fetch members';
    }
  };

  async function addMember(userIdMember: string, userEmailMember: string, checklistIdMembers: string)
    {
      try{
        const responseMessageRef = ref()
        const res = await axiosInstance.post('members', {
        idMember: 0,
        userIdMember,
        userEmailMember,
        checklistIdMembers,
        isApproverMembers: userIdMember === localStorage.getItem('idUser') ? true : false
      })

      const data = res.data
    } catch (err) {
        errorRef.value = 'Failed to add member';
      } 
  }

  async function getMembersByChecklistId(checklistId: string){
    try{
      const response = await axiosInstance.get(`members/checklist/${checklistId}`)
      if (response.data.message === 'success') {
          membersRef.value = response.data.data;
        } else {
          errorRef.value = response.data.error;
        }
    } catch (err) {
      errorRef.value = 'Failed to fetch members';
    }
  }

  async function deleteMembersByChecklistId(checklistId: string){
    try{
      const response = await axiosInstance.delete(`members/checklist/${checklistId}`)
      if(response.data.message === 'success'){
        responseMessageRef.value = response.data.message
      } else {
          errorRef.value = response.data.error;
        }
    } catch (err) {
      errorRef.value = 'Failed to delete members by check list id';
    }
  }

  return {
    membersRef,
    membersRefExceptOne,
    errorRef,
    fetchMembersExceptMe,
    addMember,
    getMembersByChecklistId,
    deleteMembersByChecklistId
  }
})
-------------
import { ref } from 'vue'
import { defineStore, storeToRefs } from 'pinia'
import axiosInstance from '@/axiosInstance'
import { useWebSocket } from '@/composables/useWebSocket'
import { useCheckUserStore } from '@/stores/checkUser'
import { useMembersStore } from '@/stores/members'
import { toast } from 'vue3-toastify';

interface ChecklistItem {
  idChecklistItem: string;
  checklistsIdChecklistItem: string;
  parentIdChecklistItem?: string;
  descriptionChecklistItem: string;
  isCheckedChecklistItem: boolean;
  levelChecklistItem: number;
  levelOrderChecklistItem: number;
  updatedByChecklistItem: string | null
}

interface ResponseMessage {
  message: string;
  error?: string;
}

export const useChecklistItemStore = defineStore('checklistItem', () => {
  const checkUserStore = useCheckUserStore()
  const { fetchUser } = checkUserStore
  const { checkUser } = storeToRefs(checkUserStore)

  const memberStore = useMembersStore()
  const { getMembersByChecklistId } = memberStore
  const { membersRef } = storeToRefs(memberStore)

  const checklistItems = ref<ChecklistItem[]>([])
  const responseMessage = ref<ResponseMessage>({ message: '' })

  const areAllChildrenChecked = (parentId: string): boolean => {
    const children = checklistItems.value.filter(item => item.parentIdChecklistItem === parentId);
    return children.length > 0 && children.every(child => child.isCheckedChecklistItem);
  }

  const getParentIds = (item: ChecklistItem): string[] => {
    const parentIds: string[] = [];
    let currentItem = item;

    while (currentItem.parentIdChecklistItem) {
      parentIds.push(currentItem.parentIdChecklistItem);
      currentItem = checklistItems.value.find(i => i.idChecklistItem === currentItem.parentIdChecklistItem)!;
      if (!currentItem) break;
    }

    return parentIds;
  }

  const updateParentCheckboxes = async (item: ChecklistItem) => {
    const parentIds = getParentIds(item);

    for (const parentId of parentIds) {
      const shouldBeChecked = areAllChildrenChecked(parentId);
      const parent = checklistItems.value.find(i => i.idChecklistItem === parentId);

      if (parent && parent.isCheckedChecklistItem !== shouldBeChecked) {
        await updateChecklistItemCheckbox(
          parentId,
          shouldBeChecked,
          parent.checklistsIdChecklistItem
        );
      }
    }
  }

  async function getChecklistItemsByChecklistId(checklistId: string): Promise<void> {
    try {
      const res = await fetch(`http://localhost:9000/api/checklists/${checklistId}/items`)
      const data = await res.json()
      checklistItems.value = data
      restructureChecklistItems()
    } catch (error: any) {
      responseMessage.value = { message: 'Error fetching checklist items', error: error.message }
    }
  }

  async function addChecklistItem(
    checklistsIdChecklistItem: string,
    descriptionChecklistItem: string,
    parentLevel: number = 0,
    parentIdChecklistItem: string | undefined = undefined
  ): Promise<void> {
    try {
      const res = await axiosInstance.post('checklistitems', {
        checklistsIdChecklistItem,
        parentIdChecklistItem,
        descriptionChecklistItem,
        isCheckedChecklistItem: false,
        levelChecklistItem: parentIdChecklistItem ? parentLevel + 1 : parentLevel,
        levelOrderChecklistItem: 0
      });

      if (res.status !== 200) {
        throw new Error('Network response was not ok');
      }

      const data = res.data;

      if (data && data.idChecklistItem) {
        checklistItems.value.push(data);
        await fetchUser()
        await getMembersByChecklistId(checklistsIdChecklistItem)
        restructureChecklistItems();
      } else {
        responseMessage.value = { message: 'Invalid response from server', error: 'No checklist item added' };
      }
      
    } catch (error: any) {
      responseMessage.value = { message: 'Error adding checklist item', error: error.message };
    }
  }

  async function updateChecklistItemDescription(idChecklistItem: string, checklistsIdChecklistItem: string, descriptionChecklistItem: string): Promise<void> {
    try {
      const res = await fetch('http://localhost:9000/api/checklistitems/description', {
        method: "PUT",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          idChecklistItem,
          checklistsIdChecklistItem,
          descriptionChecklistItem
        })
      })
      const data = await res.json()
      responseMessage.value = data
      const index = checklistItems.value.findIndex(item => item.idChecklistItem === idChecklistItem)
      if (index !== -1) {
        checklistItems.value[index].descriptionChecklistItem = descriptionChecklistItem
        restructureChecklistItems()
      }
    } catch (error: any) {
      responseMessage.value = { message: 'Error updating checklist item description', error: error.message }
    }
  }

  async function deleteChecklistItem(idChecklistItem: string, checklistsIdChecklistItem: string): Promise<void> {
    try {
      const res = await fetch(`http://localhost:9000/api/checklistitems/${idChecklistItem}/${checklistsIdChecklistItem}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json"
        },
      })
      const data = await res.json()
      responseMessage.value = data
      checklistItems.value = checklistItems.value.filter(item => item.idChecklistItem !== idChecklistItem)
      restructureChecklistItems()
      toast.warning("Deleted Successfully!", {
        position: 'bottom-right',
        autoClose: 3000,
      });
    } catch (error: any) {
      responseMessage.value = { message: 'Error deleting checklist item', error: error.message }
    }
  }

    async function updateChecklistItemCheckbox(checklistItemId: string, isChecked: boolean, checklistsIdChecklistItem: string, updatedBy = localStorage.getItem('emailUser')) {
    try {
      var index = 0
      const item = checklistItems.value.find(i => i.idChecklistItem === checklistItemId);
      if (!item) {
        console.error('Checklist item not found');
        return;
      }
  
      const hasChildren = checklistItems.value.some(i => i.parentIdChecklistItem === checklistItemId);
  
      if (hasChildren && isChecked) {
        const allChildrenChecked = areAllChildrenChecked(checklistItemId);
        if (!allChildrenChecked) {
        toast.error('Sub-task(s) must be checked first.', {
          position: 'bottom-right',
          autoClose: 5000,
        });
        checklistItems.value[index].isCheckedChecklistItem = false
        return
        }
      }

     index = checklistItems.value.findIndex(item => item.idChecklistItem === checklistItemId);
      if (index !== -1) {
        checklistItems.value[index].isCheckedChecklistItem = isChecked;
        checklistItems.value[index].updatedByChecklistItem = updatedBy; 
      }
  
      const res = await axiosInstance.put('checklistitems/checkbox', {
        idChecklistItem: checklistItemId,
        isCheckedChecklistItem: isChecked,
        checklistsIdChecklistItem: checklistsIdChecklistItem,
        updatedByChecklistItem: updatedBy
      });
      const data = res.data;
  
      if (item.parentIdChecklistItem) {
        await updateParentCheckboxes(item);
      }
    } catch (error) {
      // console.error('Error updating checklist item checkbox:', error);
      throw error;
    }
  }

  async function updateChecklistItemCheckboxMultiple(id: string, isChecked: boolean) {
    try {
      const res = await axiosInstance.put(`checklistitems/checkbox/${id}/${isChecked}`)
      const data = res.data
    } catch (error) {
      console.error('Error updating checklist item checkbox:', error);
    }
  }

  async function updateSingleChecklistItemCheckbox(checklistItemId: string, isChecked: boolean, checklistsIdChecklistItem: string, updatedBy = localStorage.getItem('emailUser')) {
    const res = await axiosInstance.put('checklistitems/checkbox', {
        idChecklistItem: checklistItemId,
        isCheckedChecklistItem: isChecked,
        checklistsIdChecklistItem: checklistsIdChecklistItem,
        updatedByChecklistItem: updatedBy
      });

  }

  const restructureChecklistItems = () => {
    const sortedItems: ChecklistItem[] = []
    const map = new Map<string, ChecklistItem[]>()

    checklistItems.value.forEach(item => {
      if (!item.parentIdChecklistItem) {
        sortedItems.push(item)
      } else {
        if (!map.has(item.parentIdChecklistItem)) {
          map.set(item.parentIdChecklistItem, [])
        }
        map.get(item.parentIdChecklistItem)!.push(item)
      }
    })

    const addChildren = (parent: ChecklistItem) => {
      const children = map.get(parent.idChecklistItem) || []
      children.forEach(child => {
        sortedItems.push(child)
        addChildren(child)
      })
    }

    sortedItems.forEach(item => addChildren(item))

    checklistItems.value = sortedItems
  }

  /* const useWebsockets = () => {
    ws = new WebSocket('ws://localhost:9000/ws/socket')
    ws.onopen = () => {
      console.log('Connected to websocket')
    }
    ws.onmessage = async (event) => {
      const response = JSON.parse(event.data)
      if (response.action === 'addChecklistItem') {
        checklistItems.value.push(response.data)
        restructureChecklistItems()
      } else if (response.action === 'updateChecklistItemDescription') {
        const index = checklistItems.value.findIndex(item => item.idChecklistItem === response.data.idChecklistItem)
        if (index !== -1) {
          checklistItems.value[index].descriptionChecklistItem = response.data.descriptionChecklistItem
          restructureChecklistItems()
        }
      } else if (response.action === 'deleteChecklistItem') {
        checklistItems.value = checklistItems.value.filter(item => item.idChecklistItem !== response.data.idChecklistItem)
        restructureChecklistItems()
      } else if (response.action === 'updateChecklistItemCheckbox') {
        const updatedItem = response.data
        const index = checklistItems.value.findIndex(
          item => item.idChecklistItem === updatedItem.idChecklistItem
        )
        
        if (index !== -1) {
          const item = checklistItems.value[index]
          const hasChildren = checklistItems.value.some(i => i.parentIdChecklistItem === item.idChecklistItem)
          
          if (hasChildren && updatedItem.isCheckedChecklistItem) {
            const allChildrenChecked = areAllChildrenChecked(item.idChecklistItem)
            if (!allChildrenChecked) {
              checklistItems.value[index].isCheckedChecklistItem = false
              await updateChecklistItemCheckbox(
                item.idChecklistItem,
                false,
                item.checklistsIdChecklistItem
              )
              return
            }
          }
          
          checklistItems.value[index].isCheckedChecklistItem = updatedItem.isCheckedChecklistItem
          
          if (item.parentIdChecklistItem) {
            await updateParentCheckboxes(item)
          }
        }
      }
      console.log(response.action, response.data)
    }
    ws.onclose = () => {
      console.log('WebSocket connection closed')
      useWebsockets()
    }
    ws.onerror = (error) => {
      console.error('WebSocket error:', error)
    }
  } */

  const handleWebSocketMessage = async (response: any) => {
    if (response.action === 'addChecklistItem') {
      const userId = (checkUser.value as any)?.idUser;
      const isUserMember = membersRef.value.some((member: any) => 
        member.userIdMember === userId && 
        member.checklistIdMembers === response.data.checklistsIdChecklistItem

      );
      if (isUserMember) {
        checklistItems.value.push(response.data)
        restructureChecklistItems()
        toast.success("New Checklist Task created: " + response.data.descriptionChecklistItem, {
          position: 'bottom-right',
          autoClose: 3000,
        });
      }
    } else if (response.action === 'updateChecklistItemDescription') {
      const index = checklistItems.value.findIndex(item => item.idChecklistItem === response.data.idChecklistItem)
      if (index !== -1) {
        checklistItems.value[index].descriptionChecklistItem = response.data.descriptionChecklistItem
        restructureChecklistItems()
        toast.info("Updated Checklist name to: " + response.data.descriptionChecklistItem, {
          position: 'bottom-right',
          autoClose: 3000,
        });
      }
    } else if (response.action === 'deleteChecklistItem') {
      checklistItems.value = checklistItems.value.filter(item => item.idChecklistItem !== response.data.idChecklistItem)
      restructureChecklistItems()
    } else if (response.action === 'updateChecklistItemCheckbox') {
      const updatedItem = response.data
      const index = checklistItems.value.findIndex(
        item => item.idChecklistItem === updatedItem.idChecklistItem
      )
      checklistItems.value[index].isCheckedChecklistItem = updatedItem.isCheckedChecklistItem
      checklistItems.value[index].updatedByChecklistItem = updatedItem.updatedByChecklistItem
    }
  }

  const { ws } = useWebSocket('ws://localhost:9000/ws/socket',handleWebSocketMessage)

  return {
    checklistItems,
    responseMessage,
    getChecklistItemsByChecklistId,
    addChecklistItem,
    updateChecklistItemDescription,
    deleteChecklistItem,
    updateChecklistItemCheckbox,
    updateChecklistItemCheckboxMultiple,
    handleWebSocketMessage,
    areAllChildrenChecked,
    getParentIds,
    updateSingleChecklistItemCheckbox
  }
})
